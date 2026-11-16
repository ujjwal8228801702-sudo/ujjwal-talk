const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization"
};

const json = (data, status = 200) =>
  new Response(JSON.stringify(data), {
    status,
    headers: { ...cors, "Content-Type": "application/json" }
  });

const encoder = new TextEncoder();

function b64url(bytes) {
  let binary = "";
  for (const b of bytes) binary += String.fromCharCode(b);
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

function decodeB64url(value) {
  const padded = value.replace(/-/g, "+").replace(/_/g, "/") + "===".slice((value.length + 3) % 4);
  return Uint8Array.from(atob(padded), c => c.charCodeAt(0));
}

async function hmac(value, secret) {
  const key = await crypto.subtle.importKey("raw", encoder.encode(secret), { name: "HMAC", hash: "SHA-256" }, false, ["sign"]);
  return new Uint8Array(await crypto.subtle.sign("HMAC", key, encoder.encode(value)));
}

async function makeToken(payload, secret) {
  const head = b64url(encoder.encode(JSON.stringify({ alg: "HS256", typ: "JWT" })));
  const body = b64url(encoder.encode(JSON.stringify(payload)));
  const data = head + "." + body;
  return data + "." + b64url(await hmac(data, secret));
}

async function readToken(request, secret) {
  const value = request.headers.get("Authorization") || "";
  const token = value.startsWith("Bearer ") ? value.slice(7) : "";
  const parts = token.split(".");
  if (parts.length !== 3) return null;
  const expected = await hmac(parts[0] + "." + parts[1], secret);
  const actual = decodeB64url(parts[2]);
  if (expected.length !== actual.length || !crypto.subtle) return null;
  let diff = 0;
  for (let i = 0; i < expected.length; i++) diff |= expected[i] ^ actual[i];
  if (diff !== 0) return null;
  const payload = JSON.parse(new TextDecoder().decode(decodeB64url(parts[1])));
  if (!payload.sub || payload.exp < Math.floor(Date.now() / 1000)) return null;
  return payload;
}

async function pinHash(pin, salt) {
  const material = await crypto.subtle.importKey("raw", encoder.encode(pin), "PBKDF2", false, ["deriveBits"]);
  const bits = await crypto.subtle.deriveBits({ name: "PBKDF2", salt: encoder.encode(salt), iterations: 120000, hash: "SHA-256" }, material, 256);
  return b64url(new Uint8Array(bits));
}

function validPhone(phone) {
  return /^[6-9][0-9]{9}$/.test(String(phone || ""));
}

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") return new Response(null, { headers: cors });
    if (request.method !== "POST") return json({ error: "METHOD_NOT_ALLOWED" }, 405);
    if (!env.DB || !env.AUTH_SECRET || env.AUTH_SECRET.length < 32) {
      return json({ error: "SETUP_REQUIRED", message: "D1 binding DB और 32+ अक्षरों का AUTH_SECRET सेट करें।" }, 500);
    }

    const url = new URL(request.url);
    try {
      const data = await request.json();

      if (url.pathname === "/register") {
        const phone = String(data.phone || "");
        const name = String(data.name || "").trim();
        const pin = String(data.pin || "");
        if (!validPhone(phone) || name.length < 2 || name.length > 35 || !/^[0-9]{6}$/.test(pin)) {
          return json({ error: "INVALID_DETAILS", message: "सही मोबाइल नंबर, 2–35 अक्षर का नाम और 6 अंकों का PIN डालें।" }, 400);
        }
        const exists = await env.DB.prepare("SELECT id FROM users WHERE phone = ?").bind(phone).first();
        if (exists) return json({ error: "ACCOUNT_EXISTS", message: "इस नंबर का अकाउंट पहले से है। लॉगिन करें।" }, 409);
        const id = crypto.randomUUID();
        const salt = crypto.randomUUID();
        const hash = await pinHash(pin, salt);
        await env.DB.prepare("INSERT INTO users (id, phone, name, pin_salt, pin_hash, created_at) VALUES (?, ?, ?, ?, ?, ?)").bind(id, phone, name, salt, hash, Date.now()).run();
        const token = await makeToken({ sub: id, exp: Math.floor(Date.now() / 1000) + 60 * 60 * 24 * 30 }, env.AUTH_SECRET);
        return json({ token, name, phone, note: "मोबाइल नंबर की SMS पुष्टि नहीं हुई है। यह PIN-आधारित शुरुआती लॉगिन है।" }, 201);
      }

      if (url.pathname === "/login") {
        const phone = String(data.phone || "");
        const pin = String(data.pin || "");
        if (!validPhone(phone) || !/^[0-9]{6}$/.test(pin)) return json({ error: "INVALID_DETAILS" }, 400);
        const user = await env.DB.prepare("SELECT id, name, phone, pin_salt, pin_hash FROM users WHERE phone = ?").bind(phone).first();
        if (!user || await pinHash(pin, user.pin_salt) !== user.pin_hash) return json({ error: "LOGIN_FAILED", message: "मोबाइल नंबर या PIN गलत है।" }, 401);
        const token = await makeToken({ sub: user.id, exp: Math.floor(Date.now() / 1000) + 60 * 60 * 24 * 30 }, env.AUTH_SECRET);
        return json({ token, name: user.name, phone: user.phone });
      }

      const auth = await readToken(request, env.AUTH_SECRET);
      if (!auth) return json({ error: "UNAUTHORIZED", message: "पहले लॉगिन करें।" }, 401);

      if (url.pathname === "/search") {
        const phone = String(data.phone || "");
        if (!validPhone(phone)) return json({ error: "INVALID_PHONE" }, 400);
        const user = await env.DB.prepare("SELECT id, name, phone FROM users WHERE phone = ?").bind(phone).first();
        if (!user) return json({ found: false, message: "इस नंबर पर Ujjwal Voice अकाउंट नहीं मिला।" });
        if (user.id === auth.sub) return json({ found: false, message: "यह आपका अपना नंबर है।" });
        return json({ found: true, name: user.name, phone: user.phone });
      }

      if (url.pathname === "/token") {
        if (!env.LIVEKIT_URL || !env.LIVEKIT_API_KEY || !env.LIVEKIT_API_SECRET) return json({ error: "LIVEKIT_SETUP_REQUIRED" }, 500);
        const phone = String(data.phone || "");
        if (!validPhone(phone)) return json({ error: "INVALID_PHONE" }, 400);
        const caller = await env.DB.prepare("SELECT id, name FROM users WHERE id = ?").bind(auth.sub).first();
        const target = await env.DB.prepare("SELECT id, name FROM users WHERE phone = ?").bind(phone).first();
        if (!caller || !target || target.id === caller.id) return json({ error: "USER_NOT_FOUND" }, 404);
        const room = "uv-" + [caller.id, target.id].sort().join("-");
        const now = Math.floor(Date.now() / 1000);
        const livekitToken = await makeToken({
          iss: env.LIVEKIT_API_KEY,
          sub: caller.id,
          nbf: now - 5,
          exp: now + 60 * 60,
          video: { roomJoin: true, room, canPublish: true, canSubscribe: true, canPublishData: false }
        }, env.LIVEKIT_API_SECRET);
        return json({ url: env.LIVEKIT_URL, token: livekitToken, room, targetName: target.name });
      }

      return json({ error: "NOT_FOUND" }, 404);
    } catch (error) {
      return json({ error: "SERVER_ERROR", message: "सर्वर पर अनुरोध पूरा नहीं हुआ।" }, 500);
    }
  }
};

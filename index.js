const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type"
};

function b64url(input) {
  const bytes = typeof input === "string" ? new TextEncoder().encode(input) : input;
  let binary = "";
  bytes.forEach(b => binary += String.fromCharCode(b));
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}
async function signJwt(payload, secret) {
  const header = b64url(JSON.stringify({ alg: "HS256", typ: "JWT" }));
  const body = b64url(JSON.stringify(payload));
  const data = `${header}.${body}`;
  const key = await crypto.subtle.importKey(
    "raw", new TextEncoder().encode(secret), { name: "HMAC", hash: "SHA-256" }, false, ["sign"]
  );
  const signature = await crypto.subtle.sign("HMAC", key, new TextEncoder().encode(data));
  return `${data}.${b64url(new Uint8Array(signature))}`;
}
export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") return new Response(null, { headers: cors });
    if (request.method !== "POST") return new Response("Method not allowed", { status: 405, headers: cors });
    try {
      const { groupId, password, identity } = await request.json();
      if (groupId !== "UJJWAL10" || password !== env.GROUP_PASSWORD) {
        return Response.json({ error: "Invalid group credentials" }, { status: 401, headers: cors });
      }
      if (!identity || String(identity).length > 40) {
        return Response.json({ error: "Enter a valid member name" }, { status: 400, headers: cors });
      }
      const now = Math.floor(Date.now() / 1000);
      const token = await signJwt({
        iss: env.LIVEKIT_API_KEY,
        sub: String(identity),
        nbf: now - 5,
        exp: now + 6 * 60 * 60,
        video: { roomJoin: true, room: "ujjwal-permanent-10", canPublish: true, canSubscribe: true, canPublishData: true }
      }, env.LIVEKIT_API_SECRET);
      return Response.json({ url: env.LIVEKIT_URL, token }, { headers: cors });
    } catch (e) {
      return Response.json({ error: "Request failed" }, { status: 400, headers: cors });
    }
  }
};

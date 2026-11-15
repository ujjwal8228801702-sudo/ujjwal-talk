# Ujjwal Talk — शुरुआती Android प्रोजेक्ट

यह **पहला starter source** है, तैयार/टेस्ट किया हुआ APK नहीं। असली ऑडियो चालू करने से पहले LiveKit Cloud और Cloudflare Worker की सेटिंग जरूरी है।

## अभी तय की गई चीजें
- ऐप: Ujjwal Talk
- स्थायी ग्रुप ID: `UJJWAL10`
- LiveKit room: `ujjwal-permanent-10`
- लक्ष्य: अधिकतम 10 सदस्य
- Android package: `com.ujjwal.talk`

## जरूरी सेटअप (फ्री टियर)
1. LiveKit Cloud पर Build plan में प्रोजेक्ट बनाएँ: https://cloud.livekit.io/
2. Project URL, API Key और API Secret सुरक्षित रखें।
3. Cloudflare पर Worker बनाएँ और `worker/src/index.js` डालें।
4. Worker secrets सेट करें:
   - `LIVEKIT_URL` = LiveKit का wss URL
   - `LIVEKIT_API_KEY` = API Key
   - `LIVEKIT_API_SECRET` = API Secret
   - `GROUP_PASSWORD` = अपना निजी Group Password (इसे चैट में साझा न करें)
5. Worker deploy करें। उसका URL Android में `MainActivity.kt` के `TOKEN_ENDPOINT` में डालें।
6. Android source GitHub repository में डालकर Actions चलाएँ। `Ujjwal-Talk-debug-apk` artifact से APK डाउनलोड करें।

## महत्वपूर्ण
- इस starter में UI और token-worker का आधार है; इसे अभी production-ready न मानें।
- Push-to-talk के लिए अगली coding pass में Compose pointer press/release को `setTalking(true/false)` से जोड़ना, room disconnect handling, सदस्य सूची, reconnect और Android foreground-service lifecycle को पूरा करना होगा।
- Android के microphone foreground-service नियमों और बैटरी optimizations के कारण हर फोन पर force-stop के बाद auto-reconnect की गारंटी नहीं होती।
- Group password को APK में hard-code न करें। Worker secrets में ही रखें।
- LiveKit Free Build की सीमाएँ हैं; official pricing/quotas बदल सकती हैं। 10 लोगों के इस्तेमाल में मिनटों की खपत ध्यान रखें।

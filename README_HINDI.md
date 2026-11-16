# Ujjwal Voice — शुरुआती 1-to-1 कॉलिंग प्रोजेक्ट

यह ujjwal-voice-ui ब्रांच का शुरुआती कोड है। Android ऐप की हिंदी स्क्रीन और Cloudflare Worker API का आधार तैयार है। इसे अभी तैयार/production-ready calling app न मानें।

## इस चरण में क्या जोड़ा गया
- अलग Ujjwal Voice Android स्क्रीन
- मोबाइल नंबर और नाम से अकाउंट बनाने का API
- 6 अंकों के PIN से लॉगिन API
- रजिस्टर्ड नंबर खोजने का API
- दो यूज़र के बीच निजी LiveKit room token देने का API
- Cloudflare D1 database schema

## जरूरी सुरक्षा सूचना
SMS OTP नहीं है। नंबर का मालिकाना हक सत्यापित नहीं होता; इसलिए कोई व्यक्ति किसी दूसरे का नंबर पहले रजिस्टर कर सकता है। इस शुरुआती PIN-आधारित prototype को संवेदनशील बातचीत के लिए इस्तेमाल न करें। असली उपयोग से पहले OTP/विश्वसनीय verification, rate limiting, incoming-call notification और abuse protection जोड़ना जरूरी है।

## Cloudflare Worker सेटअप
1. Cloudflare Dashboard में Workers & Pages खोलें और नया Worker बनाएँ।
2. worker/index.js का पूरा कोड Worker में डालें और Save/Deploy करें।
3. D1 में नया database बनाएँ, उदाहरण नाम: ujjwal-voice.
4. D1 Console में worker/schema.sql का SQL चलाएँ।
5. Worker Settings → Bindings में D1 database binding जोड़ें: Variable name DB; Database ujjwal-voice.
6. Worker Settings → Variables and Secrets में ये secrets जोड़ें: AUTH_SECRET (कम-से-कम 32 अक्षरों का नया random secret), LIVEKIT_URL, LIVEKIT_API_KEY, LIVEKIT_API_SECRET.
7. Worker deploy करें। API routes: /register, /login, /search, /token.

## अभी बाकी
- Android स्क्रीन को इन API routes से जोड़ना
- लॉगिन session सुरक्षित रूप से सहेजना
- LiveKit SDK से outgoing/incoming call UI और call controls
- incoming-call notification, call timeout, busy/reject/end flow
- OTP verification और production security
- GitHub Actions build सफल होने के बाद APK की जाँच

## ₹0 लक्ष्य
Cloudflare और LiveKit के free tiers की उपलब्धता/सीमाएँ बदल सकती हैं। Free tier की गारंटी या अनलिमिटेड कॉलिंग का वादा नहीं है। SMS OTP के बिना नंबर verification नहीं होगा.

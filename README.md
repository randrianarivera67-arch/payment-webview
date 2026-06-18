# Payment WebView — Ultra Premium

Pejy **payment (WebView)** ho an'ny **Dépôt / Retrait** (Mobile Money MG).
Ny *montant*, *numéro* sy *code USSD* dia avy amin'ny ordre (`Retrait`) ao amin'ny backend.
Ny bouton **« COMPOSER LE CODE »** dia manokatra ny USSD amin'ny finon'ny client →
ny client no mampiditra ny PIN-ny.

## Rafitra
```
public/index.html              ← ny site (single-file, deploy any Vercel)
backend-additions/             ← route kely hoampiana ao amin'ny backend
android/PaymentWebViewActivity.kt  ← wrapper APK (ACTION_CALL USSD + bridge)
vercel.json
```

## 1. Deploy (Vercel)
- Push ity repo ity any GitHub.
- Vercel → Import repo → Deploy (outputDirectory = `public`, efa voafaritra).

## 2. Ampifandraiso amin'ny backend
Ao amin'ny `public/index.html`, anatin'ny `<script>` (CFG) :
```js
USE_MOCK: false,
API_BASE: "https://<anaranao>.onrender.com",
```
Mila ampiana ao amin'ny backend ny route `GET /api/retrait/:id`
(jereo `backend-additions/retrait-get-by-id.js` → apetraka ao `routes/retrait.js`).

Token JWT: `?token=<jwt>` na bridge Android. Order: `?order=<_id>` na bridge.

## 3. Android (APK)
- Adikao `android/PaymentWebViewActivity.kt`, ovay ny `PAYMENT_URL` = URL Vercel.
- `AndroidManifest.xml`: `<uses-permission android:name="android.permission.CALL_PHONE"/>`
- Sokafy: `Intent(... ).putExtra("orderId", retrait._id).putExtra("token", token)`.

## Test haingana (mock)
Sokafy `public/index.html` ao amin'ny navigateur:
`?type=depot&op=mvola` na `?type=retrait&op=orange`

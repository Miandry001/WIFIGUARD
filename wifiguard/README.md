# WifiGuard

App Android + serveur: ny mpampiasa manana compte (anarana + mot de passe) ihany no mahazo ny mot de passe WiFi Starlink, ary mifandray ho azy ny telefaona amin'ny WiFi **afenina**.

## Fiasana

1. Ny Starlink dia misy SSID **afenina** sy WPA2/WPA3 mot de passe lava.
2. Ny mot de passe WiFi dia tahiry ao amin'ny **serveur** fa tsy ao anaty APK.
3. Ny app mangataka anarana + mot de passe, mandefa any amin'ny serveur (amin'ny data mobile na internet hafa), mandray ny SSID/mot de passe, ary mifandray.
4. Hanala olona: `node admin.js disable <anarana>`, avy eo ovao ny mot de passe WiFi (`set-wifi`) ary Starlink.

## 1. Serveur (ao amin'ny VPS/hébergeur misy HTTPS)

```bash
cd server
node admin.js set-wifi "SSID-NAO" "MotDePasseWiFiLava123"
node admin.js add rakoto "mdp-an-i-rakoto"
node admin.js list
node server.js            # port 3000 (PORT=8080 node server.js)
```

Ataovy **HTTPS** (Caddy, nginx + Let's Encrypt, Cloudflare Tunnel, Render, Railway, ...). Aza ampiasaina HTTP amin'ny famokarana.

## 2. App Android

1. Sokafy ny lahatahiry `android/` amin'ny **Android Studio** (Hedgehog na vaovao kokoa).
2. Ao amin'ny `app/build.gradle.kts`, soloy ny `https://CHANGE-ME.example.com` amin'ny adiresy serveur-nao.
3. **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. APK: `app/build/outputs/apk/debug/app-debug.apk`. Alefaso any amin'ny telefaona (mamela "app tsy fantatra" aloha).

Fitsapana amin'ny emulateur: `SERVER_URL = "http://10.0.2.2:3000"` (avelan'ny `network_security_config.xml`).

## Fanaovana APK tsy mila Android Studio (GitHub)

1. Manaova compte GitHub, mamorona repository **private**, ary ampidiro ny votoatin'ny lahatahiry `wifiguard/` (miaraka amin'ny `.github/`).
2. Any amin'ny **Actions > Build APK > Run workflow**.
3. Rehefa vita (3-6 minitra), alaivo ny **WifiGuard-apk** ao amin'ny "Artifacts": ao ny `app-debug.apk`.

## Fanapetrahana sy fizarana ny APK

- Azo zaraina amin'ny **WhatsApp, Xender, Bluetooth, USB** na rohy, toy ny fizarana APK hafa rehetra.
- Ao amin'ny telefaona: sokafy ny APK, ary raha angatahana, **avelao ny "Install unknown apps"** ho an'ilay app nanokatra azy (WhatsApp, Files...).
- Ho an'ny fizarana lavitra (Play Store na APK sonia): mila **signature release** (keystore) ary `assembleRelease`. Ny debug APK dia mety tsara ho an'ny ekipa kely.
- Aza zarao any ivelan'ny ekipa ny APK raha misy fampahalalana miafina ao anatiny (tsy misy mot de passe WiFi ao anatiny, fa ny adiresy serveur ihany).

## Fanamarihana ilaina

- **Android 10+ ihany** (minSdk 29). iPhone: tsy azo atao ny mampifandray WiFi afenina ho azy amin'ny fomba ankehitriny.
- Safidy "ho an'ity app ity ihany": ny app ihany no mampiasa ny internet amin'ny WiFi. Raha tsy voatondro, ny telefaona manontolo no mampiasa azy, saingy mila fanekena indray mandeha.
- Ny SSID afenina dia tsy fiarovana tanteraka (azo hita amin'ny scanner). Ny fiarovana tena izy dia ny mot de passe + ny fanovana azy rehefa misy olona esorina.
- Ho an'ny fifehezana tsirairay avy (tsy mila manova mot de passe isaky ny mamoaka olona): ampio routeur faharoa miaraka amin'ny WPA2-Enterprise/RADIUS na VPN WireGuard.
- Ny olona nahazo ny mot de passe WiFi dia afaka mizara azy. Tsy azo ihany izany ho tsy azo atao tanteraka raha tsy RADIUS/VPN.

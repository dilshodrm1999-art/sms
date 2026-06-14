# Amir Temur: Buyuk Yurishlar

2D tarixiy strategiya o'yini — Amir Temurning yoshligidan vafotigacha bo'lgan buyuk yurishlari.
To'liq **o'zbekcha** (lotin alifbosi), **offline**, Android uchun.

> Janr: 2D Strategy + Historical Campaign · Platforma: Android 8.0+ (API 26+) · Engine: Android (Kotlin, Canvas)

---

## ⬇️ Tayyor APK ni yuklab olish

APK avtomatik tarzda GitHub Actions orqali quriladi va **Releases** bo'limiga joylanadi:

1. Repozitoriyning **Releases** sahifasiga o'ting:
   `https://github.com/dilshodrm1999-art/sms/releases`
2. Eng so'nggi `latest` relizidan **`AmirTemur-BuyukYurishlar.apk`** faylini yuklab oling.
3. Telefoningizda **Sozlamalar → Xavfsizlik → Noma'lum manbalardan o'rnatish**ga ruxsat bering.
4. APK ni o'rnating va o'ynashni boshlang!

> Eslatma: APK debug imzo bilan quriladi, shu sababli to'g'ridan-to'g'ri telefoningizga o'rnatsa bo'ladi.
> Build holatini **Actions** bo'limidan kuzatishingiz mumkin.

---

## 🎮 O'yin haqida

O'yinchi Amir Temur nomidan boshqaradi: qo'shin to'playdi, strategiya tanlaydi,
xaritadan yurish boshlaydi va tarixiy janglarni o'tkazadi.

### Bosqichlar (real tarixga asoslangan)
| # | Yurish | Yil | Dushman |
|---|--------|-----|---------|
| 0 | Prolog — Kesh | 1360-yillar | Mahalliy raqib amirlar |
| 1 | Movarounnahr uchun kurash | 1361–1370 | Amirlar va mo'g'ullar |
| 2 | Xorazm yurishi | 1371–1388 | Xorazm hukmdorlari |
| 3 | Oltin O'rda urushi | 1391 | To'xtamishxon |
| 4 | Hindiston yurishi | 1398 | Dehli sultonligi |
| 5 | Usmonlilar bilan urush | 1402 | Sulton Boyazid I |
| F | So'nggi yurish | 1405 | Xitoy yo'nalishi (vafot) |

### Mexanikalar
- **Askar turlari:** otliq, piyoda, kamonchi, og'ir zirhli, qamal quroli
- **Strategiyalar:** frontal hujum, yashirin hujum, qanotdan aylanish, mudofaa, qamal
- Strategiya va qo'shin tarkibi jang natijasiga ta'sir qiladi
- Har bir yurishdan oldin **sinematik** + o'zbekcha subtitr
- Har bir g'alabadan keyin **tarixiy ma'lumot**

### Dizayn
- Ranglar: qora, oltin, jigarrang (medieval turkiy uslub)
- Yog'och uslubidagi tugmalar, parchment subtitrlar
- Barcha grafika **dasturiy (vektor/canvas)** chiziladi — copyright muammosiz, hajm kichik
- Ovoz effektlari **dasturiy generatsiya** qilinadi (tashqi fayllarsiz)

---

## 🛠 Mahalliy qurish (ixtiyoriy)

Android SDK o'rnatilgan bo'lsa:

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Talablar: JDK 17, Android SDK (platform 34, build-tools 34.0.0).

---

## 📦 Texnik
- Engine: Android SDK + Kotlin (tashqi runtime kutubxonalarsiz)
- minSdk 26 (Android 8.0+), targetSdk 34
- APK hajmi: ~2–3 MB (300 MB dan ancha kichik)
- Offline ishlaydi, internet ruxsati talab qilinmaydi

## 📄 Play Market
Do'kon uchun matnlar va maxfiylik siyosati: [`docs/`](docs/) papkasida.

## 🔌 Reklama (monetizatsiya)
Hozircha reklama **namuna (placeholder)** sifatida ulangan (`AdScreen.kt`).
Haqiqiy AdMob (Rewarded/Interstitial) ulamoqchi bo'lsangiz, o'z Ad Unit ID laringizni
qo'shing. Reklama faqat ixtiyoriy "bonus" tugmasi orqali ko'rsatiladi (spam emas).

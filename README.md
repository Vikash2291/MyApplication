# GK Quiz – Hindi & English

A bilingual (English / हिन्दी) general-knowledge quiz app built on a production-ready Android
architecture (Kotlin, Compose, Hilt, Room, DataStore, Firebase Auth, AdMob + UMP).
Application id: `com.alfred.myapplication`.

## 🧠 Quiz features

- **Offline question bank**: 300+ fact-checked multiple-choice questions (4 options, one correct
  answer, short explanation) in English and Hindi, bundled in `app/src/main/assets/questions.json`
  and imported into Room on first launch. Categories: India GK, World GK, Science, History,
  Geography, Sports, Current Affairs (settled, dated facts only) and Computers.
- **Quiz**: 10 questions, 20 seconds each, instant right/wrong feedback with explanation,
  bookmark any question. Least-seen questions are served first.
- **Daily Quiz**: the same 10 questions for everyone on a given date
  (`DailyQuizSelector`, deterministic), with a daily streak stored in DataStore.
- **Result**: score, points earned, full answer review with explanations.
- **Bookmarks**, **Progress/Stats** (accuracy, coverage and best score per category, streaks).
- **Language toggle** in Settings (DataStore): UI strings come from `values/` and `values-hi/`,
  question text from the per-question translations.
- **Points**: 10 per correct answer, +20 for a perfect quiz, +10 for completing the daily quiz.
- Sign in with email (Firebase) or continue as a guest; in-app account deletion wipes quiz data too.

### Ads policy in the quiz

- Rewarded (opt-in only): "50:50 hint" or "+10 s time" during a question; the timer pauses while
  the video plays.
- Interstitial: only at the break between quizzes (Result → Home), using the existing pacing
  (≥ 2 user actions and ≥ 2 minutes between interstitials).
- Banner: Home and Result only, in a fixed bottom slot. Never on the Quiz screen near answers.

### Editing the question bank

Questions live in `tools/questions/bank_*.py` (correct option first). Regenerate the asset with:

```bash
python3 tools/questions/build_bank.py   # validates, shuffles options deterministically, writes JSON
```

Append new questions at the end of a list (ids stay stable) and bump `BANK_VERSION` so
installed apps re-import the bank. `QuestionBankTest` guards the asset in CI.

## 🏗 Architecture

```
MyApplication/
├── app/                          # Main application module
├── core/
│   ├── common/                   # Shared utilities & constants
│   ├── database/                 # Room database + DataStore
│   ├── network/                  # Retrofit + OkHttp + Coil
│   └── testing/                  # Test utilities & fakes
├── feature/                      # Feature modules (future)
└── .github/workflows/            # CI/CD pipelines
```

### Clean Architecture Layers

```
Domain Layer (Pure Kotlin)
├── model/           # Domain entities (User, AuthResult, Result)
├── repository/      # Repository interfaces
└── usecase/         # Business logic (LoginUseCase, RegisterUseCase, etc.)

Data Layer
├── local/           # Room database, DataStore preferences
├── remote/          # Retrofit API service, DTOs
├── repository/      # Repository implementations
└── ads/             # AdMob Mediation (Unity bidding), UMP, app open

Presentation Layer
├── navigation/      # Navigation graph & navigator
├── ui/
│   ├── screens/     # Compose screens (Login, Home, Settings, etc.)
│   ├── components/  # Reusable Compose components
│   └── theme/       # Material3 theming
└── viewmodel/       # Hilt ViewModels (Auth, Home, Settings)
```

## 🛠 Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Kotlin 2.2 |
| **Build** | Gradle 8.14 + AGP 8.13 (KTS) + Version Catalogs |
| **Architecture** | Clean Architecture + MVVM |
| **DI** | Hilt 2.48 |
| **Async** | Coroutines + Flow |
| **Database** | Room 2.6.1 + DataStore 1.0 |
| **Network** | Retrofit 2.11 + OkHttp 4.12 + Kotlinx Serialization |
| **Images** | Coil 2.5 |
| **UI** | Jetpack Compose + Material3 + Navigation Compose |
| **Auth** | Firebase Auth |
| **Analytics/Crashlytics** | Firebase Analytics + Crashlytics |
| **Ads** | AdMob Mediation (Unity Ads as bidding source) + UMP |
| **Testing** | JUnit 4, MockK, Truth, Coroutines Test, Compose UI Test |
| **Static Analysis** | Detekt |
| **CI/CD** | GitHub Actions |

## 📱 Features

- ✅ **Authentication** - Email/password with Firebase Auth + backend sync
- ✅ **User Profile** - Display name, photo, email verification
- ✅ **Account deletion** - In-app (Profile, or Settings → Data), with password re-auth when Firebase requires a recent login; wipes Room + DataStore
- ✅ **Offline-first** - Room database with reactive Flow streams
- ✅ **Preferences** - DataStore (type-safe, async)
- ✅ **Ads Monetization** - AdMob Mediation with Unity bidding (rewarded, interstitial, app open, banner)
- ✅ **Consent Management** - UMP (User Messaging Platform) for GDPR/CCPA
- ✅ **Analytics** - Firebase Analytics + Crashlytics
- ✅ **Push Notifications** - FCM ready
- ✅ **Background Sync** - WorkManager (Hilt worker, every 6h)
- ✅ **Theming** - Light/Dark/System + Material3 dynamic colors
- ✅ **CI/CD** - Automated lint, test, build, release

## 🚀 Getting Started

### Prerequisites

- Android Studio Koala (2024.1.1)+
- JDK 17
- Android SDK 36 (compileSdk/targetSdk 36, minSdk 24)

### Setup

```bash
git clone https://github.com/Vikash2291/MyApplication.git
cd MyApplication
./gradlew assembleDebug      # builds with Google test ad IDs, Firebase disabled
./gradlew testDebugUnitTest  # unit tests
```

The app builds and runs **without** any keys:
- Ads use Google's official test ad unit IDs.
- Without `app/google-services.json`, Firebase plugins are skipped and login shows
  "Firebase is not configured".

### Going to production

| What | Where |
|------|-------|
| Firebase (Auth, Analytics, Crashlytics, FCM) | Create a Firebase project, enable Email/Password auth, put `google-services.json` in `app/` |
| AdMob App ID | `./gradlew assembleRelease -PADMOB_APP_ID=ca-app-pub-xxx~yyy` (or edit `manifestPlaceholders` in `app/build.gradle.kts`) |
| AdMob ad unit IDs | Debug: Google test IDs in `app/src/main/res/values/strings.xml`. Release: `app/src/release/res/values/ads.xml` (app open unit is still a TODO test ID) |
| Unity Ads | No app config: add Unity as a **bidding** source in an AdMob mediation group (adapter `com.google.ads.mediation:unity`) |
| Backend (optional) | `API_BASE_URL` in `app/build.gradle.kts`; all backend calls are best-effort |
| Release signing | `release.keystore` in project root + `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` env vars or `keystore.properties` |

### Ads strategy built in
- **AdMob Mediation** is the only ad layer. Unity Ads joins as a **bidding** source via the AdMob adapter (Unity's waterfall placements were retired in Jan 2026); no Unity code or placement IDs live in the app.
- **UMP consent** is requested on every launch; Mobile Ads is initialised and ads are loaded only when `canRequestAds()` is true. Settings shows **Privacy options** when UMP says they are required.
- **Rewarded video** (main format) on Home: opt-in, grants reward points.
- **Interstitial** only at natural breaks after a user action (leaving Home), max one per 2 user actions **and** at least 2 minutes apart; never on launch or exit.
- **App open** ad when returning to the app from the background: max once per 4 hours, never on the first cold start, never before consent, never over another full-screen ad.
- **Adaptive banner** in a fixed-height reserved slot at the bottom of Home, separated from buttons.
- Rewarded and interstitial ads are preloaded and reloaded right after dismissal.
- Debug builds always use Google test ad units; never click live ads.

## 🧪 Testing

```bash
# Unit tests (JVM)
./gradlew test

# Instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# All tests + lint + detekt
./gradlew check
```

### Test Structure

```
app/src/test/           # Unit tests (ViewModels, UseCases, Repositories)
app/src/androidTest/    # Instrumented tests (UI, Database, Hilt)
core/testing/           # Shared test utilities & fakes
```

## 🏗 Build Variants

| Variant | Description |
|---------|-------------|
| `debug` | Debuggable, test ads, `.debug` suffix |
| `release` | Minified, signed, ProGuard optimized |

## 📦 CI/CD Pipeline

GitHub Actions workflow (`.github/workflows/ci.yml`):

1. **Lint & Static Analysis** - Detekt + Android Lint
2. **Unit Tests** - JVM tests with coverage
3. **Instrumented Tests** - Emulator tests (API 34)
4. **Build** - Debug APK + Release APK/AAB
5. **Dependency Check** - OWASP vulnerability scan
6. **Release** - Auto-create GitHub Release on main push

### Required GitHub Secrets

| Secret | Description |
|--------|-------------|
| `RELEASE_KEYSTORE` | Base64-encoded release keystore |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

## 📁 Project Structure Details

### Core Modules

- **core:common** - Shared constants, extensions, utilities
- **core:database** - Room entities/DAOs, DataStore preferences, TypeConverters
- **core:network** - Retrofit setup, OkHttp interceptors, Coil image loading
- **core:testing** - Test utilities, fake implementations, Compose test rules

### App Module Package Structure

```
com.example.myapplication
├── MyApplication.kt              # Hilt Application class
├── di/                           # Hilt modules
├── data/
│   ├── local/                    # Database, DataStore, DAOs
│   ├── remote/                   # API service, DTOs, NetworkModule
│   ├── repository/               # Repository implementations
│   └── ads/                      # AdsManager + AppOpenAdManager (AdMob Mediation)
├── domain/
│   ├── model/                    # Domain entities
│   ├── repository/               # Repository interfaces
│   └── usecase/                  # Business logic use cases
├── presentation/
│   ├── navigation/               # Navigation graph, AppNavigator
│   ├── ui/
│   │   ├── screens/              # Compose screens
│   │   ├── components/           # Reusable components
│   │   ├── theme/                # Material3 theme
│   │   └── splash/               # Splash screen
│   └── viewmodel/                # Hilt ViewModels
└── utils/                        # BootReceiver, SyncService, NetworkBoundResource
```

## 🎨 Theming

Material3 with custom:
- Color schemes (Light/Dark)
- Typography scale
- Shape system
- Dynamic color support (Android 12+)

Switch themes in Settings → Appearance → Theme (System/Light/Dark).

## 📊 Ads Integration

### AdMob Mediation + Unity Ads bidding

See "Ads strategy built in" above. Dashboard steps:
1. AdMob: create GDPR and US-states messages (Privacy & messaging), and an App open ad unit (put its ID in `app/src/release/res/values/ads.xml`).
2. Unity: create a project in "I will use Mediation → AdMob" mode and set the COPPA designation.
3. AdMob: Mediation → mediation group per format → add **Unity Ads (bidding)**.
4. Publish `app-ads.txt` (AdMob + Unity lines) on the developer website listed on Google Play.

## 🔒 Security

- ProGuard/R8 full optimization
- Network Security Config (cleartext disabled)
- Encrypted DataStore for sensitive prefs
- Firebase App Check ready
- Certificate pinning ready (add to OkHttp)

## 📈 Performance

- Baseline Profiles ready (add `baseline-prof.txt`)
- R8 full mode + library optimization
- Compose compiler metrics enabled
- Lazy loading for non-critical features
- Image caching with Coil (memory + disk)

## 🤝 Contributing

1. Fork repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Run `./gradlew check` locally
4. Commit changes (`git commit -m 'Add amazing feature'`)
5. Push branch (`git push origin feature/amazing-feature`)
6. Open Pull Request

## 📄 License

```
MIT License

Copyright (c) 2024 MyApplication

Permission is hereby granted...
```

## 🙏 Acknowledgments

- [Android Architecture Components](https://developer.android.com/jetpack/guide)
- [Hilt Dependency Injection](https://dagger.dev/hilt/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Unity LevelPlay](https://unity.com/products/levelplay)
- [Firebase](https://firebase.google.com/)

---

**Built with ❤️ using modern Android development practices.**
# MyApplication

Production-ready Android application built with modern architecture and best practices.

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
└── ads/             # AdMob + Unity LevelPlay mediation

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
| **Language** | Kotlin 1.9.23 |
| **Build** | Gradle 8.7 (KTS) + Version Catalogs |
| **Architecture** | Clean Architecture + MVVM |
| **DI** | Hilt 2.48 |
| **Async** | Coroutines + Flow |
| **Database** | Room 2.6.1 + DataStore 1.0 |
| **Network** | Retrofit 2.11 + OkHttp 4.12 + Kotlinx Serialization |
| **Images** | Coil 2.5 |
| **UI** | Jetpack Compose + Material3 + Navigation Compose |
| **Auth** | Firebase Auth |
| **Analytics/Crashlytics** | Firebase Analytics + Crashlytics |
| **Ads** | AdMob + Unity LevelPlay (Bidding Mediation) |
| **Testing** | JUnit 4, MockK, Truth, Coroutines Test, Compose UI Test |
| **Static Analysis** | Detekt |
| **CI/CD** | GitHub Actions |

## 📱 Features

- ✅ **Authentication** - Email/password with Firebase Auth + backend sync
- ✅ **User Profile** - Display name, photo, email verification
- ✅ **Offline-first** - Room database with reactive Flow streams
- ✅ **Preferences** - DataStore (type-safe, async)
- ✅ **Ads Monetization** - AdMob + Unity LevelPlay bidding (rewarded, interstitial, banner)
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
- Android SDK 34

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
| AdMob ad unit IDs | `app/src/main/res/values/strings.xml` |
| Unity Ads (fallback) | `unity_game_id` in `strings.xml` (empty = disabled) |
| Backend (optional) | `API_BASE_URL` in `app/build.gradle.kts`; all backend calls are best-effort |
| Release signing | `release.keystore` in project root + `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` env vars or `keystore.properties` |

### Ads strategy built in
- **Rewarded video** on Home grants reward points (user-initiated, policy-safe).
- **Interstitial** only at a natural break (leaving Home for Settings/About), capped to one per 2 minutes.
- **Adaptive anchored banner** at the bottom of Home.
- **UMP consent** form before any ad request; privacy options in Settings when required (GDPR).
- AdMob primary, Unity Ads fallback; both can be toggled in Settings.

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
│   └── ads/                      # AdsManager (AdMob + Unity)
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

### AdMob + Unity LevelPlay Bidding

The `AdsManager` implements **bidding mediation** (not waterfall):
1. AdMob bidding (primary)
2. Unity Ads bidding (fallback)
3. Automatic preloading & refresh
4. Rewarded, Interstitial, Banner support
5. UMP consent for GDPR/CCPA

### To Maximize eCPM:

1. Enable **Bidding** in Unity Dashboard (not Waterfall)
2. Add mediation networks: AppLovin, AdMob, ironSource, Vungle
3. Set "Child-directed" = OFF in Unity Dashboard
4. Implement GDPR consent (`setConsent(true)`)
5. Use **Rewarded Video** at high-intent moments (revive, bonus, daily reward)
6. Test on **real device** with `testMode = false`

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
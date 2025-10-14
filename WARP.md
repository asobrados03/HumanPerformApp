# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

Repository: HumanPerformApp (Kotlin Multiplatform + Android Compose + iOS)

Common commands (Windows PowerShell shown; replace .\gradlew.bat with ./gradlew on macOS/Linux)
- Build all modules:
  - .\gradlew.bat build
- Clean build:
  - .\gradlew.bat clean
- Android debug APK and install on a connected device/emulator:
  - .\gradlew.bat :androidApp:assembleDebug
  - .\gradlew.bat :androidApp:installDebug
  - Start the app (requires adb in PATH):
    - adb shell am start -n com.humanperformcenter/com.humanperformcenter.app.MainActivity
- Android release artifacts:
  - .\gradlew.bat :androidApp:assembleRelease
  - .\gradlew.bat :androidApp:bundleRelease
- Android Lint (reports under androidApp/build/reports/lint):
  - .\gradlew.bat :androidApp:lint
  - Optionally lint the shared Android code: .\gradlew.bat :shared:lint
- Unit tests (run all):
  - Android app JVM unit tests: .\gradlew.bat :androidApp:testDebugUnitTest
  - Shared module host unit tests: .\gradlew.bat :shared:androidHostTest
- Instrumented/device tests (require running emulator/device):
  - Android app: .\gradlew.bat :androidApp:connectedDebugAndroidTest
  - Shared module device tests: .\gradlew.bat :shared:androidDeviceTest
- Run a single unit test class:
  - Android app: .\gradlew.bat :androidApp:testDebugUnitTest --tests "com.humanperformcenter.ExampleUnitTest"
  - Shared host test: .\gradlew.bat :shared:androidHostTest --tests "com.humanperformcenter.shared.ExampleUnitTest"
- Run a single instrumented test class:
  - Android app: .\gradlew.bat :androidApp:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.humanperformcenter.ExampleInstrumentedTest
  - Shared device test: .\gradlew.bat :shared:androidDeviceTest -Pandroid.testInstrumentationRunnerArguments.class=com.humanperformcenter.shared.ExampleInstrumentedTest
- iOS framework for Xcode (KMP XCFramework):
  - .\gradlew.bat :shared:assembleSharedKitXCFramework
  - Then open iosApp/ in Xcode and link the produced sharedKit.xcframework (Gradle output path is printed at the end of the task).

Environment notes
- JDK 17 is required (Android module sets sourceCompatibility/targetCompatibility to 17).
- Android SDK: compileSdk = 36, targetSdk = 34, buildToolsVersion = 35.0.0.

High-level architecture and structure
- Modules
  - :androidApp (Android Compose application)
    - UI built with Jetpack Compose and Navigation Compose.
    - Entry point: com.humanperformcenter.app.MainActivity.
    - Navigation graph is declared in androidApp/src/main/java/com/humanperformcenter/app/navigation using type-safe destinations serialized with kotlinx.serialization (see NavDestinations.kt and Navigation.kt).
    - ViewModels live in androidApp/src/main/java/com/humanperformcenter/ui/viewmodel and depend on domain UseCases.
    - Dependency wiring is centralized in com.humanperformcenter.di.AppModule, which lazily provides UseCase singletons backed by shared module repositories.
    - Local persistence for session data uses Room (Session, SessionDao, SessionDatabase, SessionRepository under androidApp/.../data).
    - Payments: integrates Google Pay and Stripe on Android. MainActivity initializes GooglePayRepository and Stripe PaymentConfiguration and passes a PaymentSheet into the composable Navigation layer.
  - :shared (Kotlin Multiplatform library)
    - Clean Architecture + Hexagonal: domain, data/persistence, and platform-specific implementations via expect/actual.
    - Domain layer (commonMain):
      - Entities and repository interfaces under domain/repository.
      - Use cases under domain/usecase orchestrate business logic and expose Result-based APIs to ViewModels.
      - Security and storage abstractions (SecureStorage, Crypto, AuthPreferences) with expect declarations resolved per platform.
    - Data layer (commonMain):
      - Ktor HttpClient configuration in data/network/ApiClient with:
        - ContentNegotiation (Kotlinx JSON, ignoreUnknownKeys).
        - Auth bearer plugin that loads/saves tokens via SecureStorage and performs token refresh against /mobile/refresh.
        - A LogoutPlugin that emits logoutEvents when the server returns unauthorized after refresh; androidApp observes this flow to route the user to the Welcome screen.
      - Repository implementations in data/persistence that map DTOs and call the API client; platform-specific pieces (e.g., Google Pay) live in androidMain.
    - Platform source sets:
      - androidMain provides actual implementations (e.g., createDataStore.android.kt, GooglePayRepository, OkHttp client for Ktor, AndroidX Security Crypto).
      - iosMain provides DarwiN client for Ktor and actual storage/crypto implementations.
  - :iosApp (SwiftUI application)
    - Native iOS shell project that consumes sharedKit.xcframework produced by the :shared module. Use Xcode to run.

Cross-cutting flows
- Session and auth
  - Tokens are stored via SecureStorage (backed by DataStore on Android). The Ktor bearer auth plugin uses them automatically.
  - On refresh failure (HTTP 401), ApiClient clears tokens and emits a logout event; androidApp’s Navigation observes ApiClient.logoutEvents and navigates to Welcome, clearing the back stack.
- Feature development path (Android)
  - Define or expand repository interfaces in shared/domain/repository and their implementations in shared/data/persistence.
  - Add or extend a UseCase in shared/domain/usecase.
  - Wire the UseCase in androidApp’s AppModule.
  - Create/update a ViewModel under androidApp/ui/viewmodel that consumes the UseCase and expose state to composables.
  - Add/screens and routes in androidApp/app/navigation and androidApp/ui/screens.

References in this repo
- Android entry activity: com.humanperformcenter.app.MainActivity
- Navigation graph root: androidApp/.../app/navigation/Navigation.kt; destinations in NavDestinations.kt
- DI module: androidApp/.../di/AppModule.kt
- Ktor client with auth + refresh: shared/.../data/network/ApiClient.kt
- Room entities/repo: androidApp/.../data
- Payment ViewModel: androidApp/.../ui/viewmodel/PaymentViewModel.kt

Notes
- There are no repository-wide Kotlin linters configured (e.g., ktlint/detekt) in build scripts. Use Android Lint tasks for code checks unless a linter is added later.


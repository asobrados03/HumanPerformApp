# Arquitectura del Módulo de Autenticación (actualizada)

> Esta documentación reemplaza supuestos legacy (como `ApiClient/AuthApiService` o inicialización de `SecureStorage` en `MainActivity`) por la implementación real actual del proyecto.

## 1. Facade de almacenamiento local — `SecureStorage` + `AuthStorageCore` + `AuthPreferences`

```mermaid
classDiagram
    class AuthLocalDataSource {
        <<interface>>
        +getAccessToken() String?
        +getRefreshToken() String?
        +saveTokens(accessToken: String, refreshToken: String) Unit
        +accessTokenFlow() Flow~String~
        +saveUser(user: User) Unit
        +userFlow() Flow~User?~
        +clearTokens() Unit
        +clear() Unit
    }

    class SecureStorage {
        <<singleton>>
        -prefs: DataStore~Preferences~
        +initialize(prefs: DataStore~Preferences~) Unit
        +getAccessToken() String?
        +getRefreshToken() String?
        +saveTokens(accessToken: String, refreshToken: String) Unit
        +accessTokenFlow() Flow~String~
        +saveUser(user: User) Unit
        +userFlow() Flow~User?~
        +clearTokens() Unit
        +clear() Unit
    }

    class AuthStorageCore {
        <<internal singleton>>
        +getAccessToken(prefs) String?
        +getRefreshToken(prefs) String?
        +saveTokens(prefs, access, refresh) Unit
        +accessTokenFlow(prefs) Flow~String~
        +saveUser(prefs, user) Unit
        +userFlow(prefs) Flow~User?~
        +clear(prefs) Unit
    }

    class AuthPreferences {
        <<singleton>>
        -KEY_ACCESS: Preferences.Key~String~
        -KEY_REFRESH: Preferences.Key~String~
        -KEY_USER_JSON: Preferences.Key~String~
        +saveTokens(prefs, access, refresh)
        +accessTokenFlow(prefs) Flow~String~
        +refreshTokenFlow(prefs) Flow~String~
        +saveUser(prefs, user)
        +userFlow(prefs) Flow~User?~
        +clear(prefs)
    }

    class Crypto {
        <<singleton>>
        +encrypt(plain: ByteArray) ByteArray
        +decrypt(cipherMessage: ByteArray) ByteArray
    }

    class Base64 {
        <<singleton>>
        +encode(bytes: ByteArray) String
        +decode(str: String) ByteArray
    }

    AuthLocalDataSource <|.. SecureStorage
    SecureStorage --> AuthStorageCore : delega operaciones
    AuthStorageCore --> AuthPreferences : persistencia cifrada
    AuthPreferences --> Crypto : cifrado/descifrado
    AuthPreferences --> Base64 : codificación/decodificación
```

---

## 2. Módulo Android App — inicialización real y navegación

```mermaid
classDiagram
    class HumanPerformApp {
        +onCreate()
    }

    class MainActivity {
        +onCreate(savedInstanceState)
    }

    class DataStoreProvider {
        <<singleton>>
        -INSTANCE: DataStore~Preferences~
        +initialize(context: Context) Unit
        +get() DataStore~Preferences~
        -createDataStore(context: Context) DataStore~Preferences~
    }

    class SecureStorage {
        <<singleton>>
        +initialize(prefs: DataStore~Preferences~) Unit
    }

    class initKoin {
        <<function>>
    }

    class Navigation {
        +Navigation(navController, onPlaySound)
    }

    HumanPerformApp --> DataStoreProvider : initialize(context)
    HumanPerformApp --> SecureStorage : initialize(DataStoreProvider.get())
    HumanPerformApp --> initKoin : inicia DI
    MainActivity --> Navigation : compose root
```

**Nota:** Actualmente `MainActivity` **no** inicializa `DataStoreProvider` ni `SecureStorage`; esa responsabilidad está centralizada en `HumanPerformApp.onCreate()`.

---

## 3. Flujo Auth KMM — `ViewModel` → `UseCase` → `Repository` → `Remote/Local`

```mermaid
classDiagram
    class AuthViewModel {
        -authUseCase: AuthUseCase
        +loginState: StateFlow
        +registerState: StateFlow
        +isChangingPassword: StateFlow
        +isResettingPassword: StateFlow
        +login(email, password)
        +register(data)
        +resetPassword(email)
        +changePassword(currentPassword, newPassword, confirmPassword, userId)
        +resetStates()
        +resetChangePasswordState()
        +resetResettingPasswordState()
    }

    class AuthUseCase {
        -authRepository: AuthRepository
        -authLocalDataSource: AuthLocalDataSource
        +login(email, password)
        +register(data)
        +resetPassword(email)
        +changePassword(currentPassword, newPassword, confirmPassword, userId)
        +logout()
    }

    class AuthRepository {
        <<interface>>
        +login(email, password)
        +register(data)
        +resetPassword(email)
        +changePassword(currentPassword, newPassword, userId)
        +logout()
    }

    class AuthRepositoryImpl {
        +login(email, password)
        +register(data)
        +resetPassword(email)
        +changePassword(currentPassword, newPassword, userId)
        +logout()
    }

    class AuthRemoteDataSource {
        <<interface>>
        +login(email, password)
        +register(data)
        +resetPassword(email)
        +changePassword(currentPassword, newPassword, userId)
        +logout()
    }

    class AuthRemoteDataSourceImpl {
        -clientProvider: HttpClientProvider
        -localDataSource: AuthLocalDataSource
    }

    class HttpClientProvider {
        <<interface>>
        +authClient: HttpClient
        +apiClient: HttpClient
        +baseUrl: String
        +logoutEvents: SharedFlow~Unit~
    }

    AuthViewModel --> AuthUseCase : invoca
    AuthUseCase --> AuthRepository : reglas de negocio
    AuthUseCase --> AuthLocalDataSource : limpieza local en logout
    AuthRepositoryImpl ..|> AuthRepository
    AuthRepositoryImpl --> AuthRemoteDataSource : IO remoto
    AuthRepositoryImpl --> AuthLocalDataSource : persistencia de sesión
    AuthRemoteDataSourceImpl ..|> AuthRemoteDataSource
    AuthRemoteDataSourceImpl --> HttpClientProvider : HTTP (Ktor)
```

---

## 4. `AuthViewModel` y UI States

```mermaid
classDiagram
    class AuthViewModel {
        -authUseCase: AuthUseCase
        +loginState: StateFlow~LoginState~
        +registerState: StateFlow~RegisterState~
        +isChangingPassword: StateFlow~ChangePasswordState~
        +isResettingPassword: StateFlow~ResetPasswordState~
        +login(email, password)
        +register(data)
        +resetPassword(email)
        +changePassword(currentPassword, newPassword, confirmPassword, userId)
        +resetStates()
        +resetChangePasswordState()
        +resetResettingPasswordState()
    }

    class LoginState {
        <<sealed>>
        Idle
        Loading
        Success(user: LoginResponse)
        Error(message: String)
    }

    class RegisterState {
        <<sealed>>
        Idle
        Loading
        Success(message: RegisterResponse)
        Error(message: String)
        ValidationErrors(fieldErrors: Map~RegisterField, String~)
    }

    class ChangePasswordState {
        <<sealed>>
        Idle
        Loading
        Success(message: String)
        Error(message: String)
    }

    class ResetPasswordState {
        <<sealed>>
        Idle
        Loading
        Success(message: String)
        Error(message: String)
    }

    AuthViewModel --> LoginState : mantiene/expone
    AuthViewModel --> RegisterState : mantiene/expone
    AuthViewModel --> ChangePasswordState : mantiene/expone
    AuthViewModel --> ResetPasswordState : mantiene/expone
```

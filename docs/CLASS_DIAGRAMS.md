# Diagramas de clases (Mermaid)

Este documento reemplaza el volcado automático anterior por diagramas **arquitectónicos y de patrones** que reflejan la estructura real del proyecto KMM.

## 1) Arquitectura general KMM (capas)

```mermaid
classDiagram
    class AuthViewModel
    class UserViewModel
    class DaySessionViewModel
    class ServiceProductViewModel
    class StripeViewModel

    class AuthUseCase
    class UserUseCase
    class DaySessionUseCase
    class ServiceProductUseCase
    class StripeUseCase

    class AuthRepository
    <<interface>> AuthRepository
    class UserRepository
    <<interface>> UserRepository
    class DaySessionRepository
    <<interface>> DaySessionRepository
    class ServiceProductRepository
    <<interface>> ServiceProductRepository
    class StripeRepository
    <<interface>> StripeRepository

    class AuthRepositoryImpl
    class UserRepositoryImpl
    class DaySessionRepositoryImpl
    class ServiceProductRepositoryImpl
    class StripeRepositoryImpl

    class ApiClient

    AuthViewModel --> AuthUseCase
    UserViewModel --> UserUseCase
    DaySessionViewModel --> DaySessionUseCase
    ServiceProductViewModel --> ServiceProductUseCase
    StripeViewModel --> StripeUseCase

    AuthUseCase --> AuthRepository
    UserUseCase --> UserRepository
    DaySessionUseCase --> DaySessionRepository
    ServiceProductUseCase --> ServiceProductRepository
    StripeUseCase --> StripeRepository

    AuthRepositoryImpl ..|> AuthRepository
    UserRepositoryImpl ..|> UserRepository
    DaySessionRepositoryImpl ..|> DaySessionRepository
    ServiceProductRepositoryImpl ..|> ServiceProductRepository
    StripeRepositoryImpl ..|> StripeRepository

    AuthRepositoryImpl --> ApiClient
    UserRepositoryImpl --> ApiClient
    DaySessionRepositoryImpl --> ApiClient
    ServiceProductRepositoryImpl --> ApiClient
    StripeRepositoryImpl --> ApiClient
```

## 2) Patrón Singleton (componentes críticos)

```mermaid
classDiagram
    class ApiClient {
      <<singleton>>
    }
    class SecureStorage {
      <<singleton>>
    }
    class AuthPreferences {
      <<singleton>>
    }
    class AuthRepositoryImpl {
      <<singleton>>
    }
    class UserRepositoryImpl {
      <<singleton>>
    }
    class DaySessionRepositoryImpl {
      <<singleton>>
    }
    class ServiceProductRepositoryImpl {
      <<singleton>>
    }
    class StripeRepositoryImpl {
      <<singleton>>
    }

    SecureStorage --> AuthPreferences
    ApiClient --> SecureStorage
```

## 3) Patrón Facade (SecureStorage)

```mermaid
classDiagram
    class SecureStorage {
      +initialize(prefs)
      +getAccessToken() String?
      +getRefreshToken() String?
      +saveTokens(access, refresh)
      +saveUser(user)
      +userFlow() Flow~User?~
      +clear()
    }

    class AuthPreferences {
      +saveTokens(...)
      +accessTokenFlow(...)
      +refreshTokenFlow(...)
      +saveUser(...)
      +userFlow(...)
      +clear(...)
    }

    class Crypto
    class Base64

    SecureStorage --> AuthPreferences : fachada simplificada
    AuthPreferences --> Crypto : cifrado/descifrado
    AuthPreferences --> Base64 : codificación
```

## 4) Patrón Strategy (expect/actual de KMM)

```mermaid
classDiagram
    class Crypto_Pays {
      <<expect>>
      +sha256(input)
      +encryptAES(input, key)
    }

    class Crypto_Pays_Android {
      <<actual>>
    }

    class Crypto_Pays_iOS {
      <<actual>>
    }

    Crypto_Pays_Android ..|> Crypto_Pays
    Crypto_Pays_iOS ..|> Crypto_Pays
```

## 5) Patrón Repository

```mermaid
classDiagram
    class AuthUseCase
    class UserUseCase
    class DaySessionUseCase
    class ServiceProductUseCase
    class StripeUseCase

    class AuthRepository
    <<interface>> AuthRepository
    class UserRepository
    <<interface>> UserRepository
    class DaySessionRepository
    <<interface>> DaySessionRepository
    class ServiceProductRepository
    <<interface>> ServiceProductRepository
    class StripeRepository
    <<interface>> StripeRepository

    class AuthRepositoryImpl
    class UserRepositoryImpl
    class DaySessionRepositoryImpl
    class ServiceProductRepositoryImpl
    class StripeRepositoryImpl

    AuthUseCase --> AuthRepository
    UserUseCase --> UserRepository
    DaySessionUseCase --> DaySessionRepository
    ServiceProductUseCase --> ServiceProductRepository
    StripeUseCase --> StripeRepository

    AuthRepositoryImpl ..|> AuthRepository
    UserRepositoryImpl ..|> UserRepository
    DaySessionRepositoryImpl ..|> DaySessionRepository
    ServiceProductRepositoryImpl ..|> ServiceProductRepository
    StripeRepositoryImpl ..|> StripeRepository
```

## 6) Observer + State + Command (UI reactiva)

```mermaid
classDiagram
    class AuthViewModel
    class DaySessionViewModel
    class UserViewModel

    class LoginState
    class RegisterState
    class ChangePasswordState
    class ResetPasswordState
    class DailySessionsUiState
    class UpdateState
    class DeleteUserState

    class BookingEvent
    class AssignEvent
    class UnassignEvent

    AuthViewModel --> LoginState
    AuthViewModel --> RegisterState
    AuthViewModel --> ChangePasswordState
    AuthViewModel --> ResetPasswordState

    DaySessionViewModel --> DailySessionsUiState
    DaySessionViewModel --> BookingEvent

    UserViewModel --> UpdateState
    UserViewModel --> DeleteUserState
    UserViewModel --> AssignEvent
    UserViewModel --> UnassignEvent
```

## 7) Jerarquía de errores de dominio (State robusto)

```mermaid
classDiagram
    class ChangePasswordException
    class CurrentRequired
    class NewRequired
    class ConfirmRequired
    class TooShort
    class NotMatching
    class NoNumber
    class NoUppercase
    class NoLowercase
    class SameAsCurrent
    class ContainsSpace
    class RepoFailure

    class BookingDomainException
    class WeeklyLimitExceeded
    class TotalSessionsLimitExceeded
    class DuplicateBooking
    class GenericBookingFailure

    class CryptoException
    class DecryptionFailed

    CurrentRequired --|> ChangePasswordException
    NewRequired --|> ChangePasswordException
    ConfirmRequired --|> ChangePasswordException
    TooShort --|> ChangePasswordException
    NotMatching --|> ChangePasswordException
    NoNumber --|> ChangePasswordException
    NoUppercase --|> ChangePasswordException
    NoLowercase --|> ChangePasswordException
    SameAsCurrent --|> ChangePasswordException
    ContainsSpace --|> ChangePasswordException
    RepoFailure --|> ChangePasswordException

    WeeklyLimitExceeded --|> BookingDomainException
    TotalSessionsLimitExceeded --|> BookingDomainException
    DuplicateBooking --|> BookingDomainException
    GenericBookingFailure --|> BookingDomainException

    DecryptionFailed --|> CryptoException
```

## 8) Composición DI con Koin

```mermaid
classDiagram
    class appModule
    class platformModule

    class AuthRepository
    class UserRepository
    class DaySessionRepository
    class ServiceProductRepository
    class StripeRepository

    class AuthRepositoryImpl
    class UserRepositoryImpl
    class DaySessionRepositoryImpl
    class ServiceProductRepositoryImpl
    class StripeRepositoryImpl

    class AuthUseCase
    class UserUseCase
    class DaySessionUseCase
    class ServiceProductUseCase
    class StripeUseCase

    class AuthViewModel
    class UserViewModel
    class DaySessionViewModel
    class ServiceProductViewModel
    class StripeViewModel
    class UserStatsViewModel

    appModule --> AuthRepository
    appModule --> UserRepository
    appModule --> DaySessionRepository
    appModule --> ServiceProductRepository
    appModule --> StripeRepository

    AuthRepositoryImpl ..|> AuthRepository
    UserRepositoryImpl ..|> UserRepository
    DaySessionRepositoryImpl ..|> DaySessionRepository
    ServiceProductRepositoryImpl ..|> ServiceProductRepository
    StripeRepositoryImpl ..|> StripeRepository

    appModule --> AuthUseCase
    appModule --> UserUseCase
    appModule --> DaySessionUseCase
    appModule --> ServiceProductUseCase
    appModule --> StripeUseCase

    appModule --> AuthViewModel
    appModule --> UserViewModel
    appModule --> DaySessionViewModel
    appModule --> ServiceProductViewModel
    appModule --> StripeViewModel
    appModule --> UserStatsViewModel

    platformModule <.. appModule
```

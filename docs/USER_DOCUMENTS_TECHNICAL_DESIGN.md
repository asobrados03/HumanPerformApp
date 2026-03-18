# Diseño técnico cerrado: área de usuario y documentos

## Objetivo

Cerrar la fase de diseño antes de tocar código para dividir la responsabilidad hoy concentrada en `UserRepository`, `UserUseCase` y `UserViewModel` en contratos, casos de uso y viewmodels pequeños, con una frontera explícita entre **usuario**, **documentos** y **sesión**.

Este documento toma como base los identificadores existentes:

- `UserRepository`
- `UserUseCase`
- `UserViewModel`
- `DocumentScreen`
- `AuthRepositoryImpl.logout()`

El resultado de esta propuesta debe eliminar la ambigüedad sobre **dónde vive cada método actual** y fijar la arquitectura objetivo para la migración.

---

## 1. Diagnóstico del estado actual

### 1.1 Problema principal

Actualmente el área de usuario mezcla en una sola cadena (`UserRepository` -> `UserUseCase` -> `UserViewModel`) responsabilidades de:

- perfil de usuario
- eliminación de cuenta
- logout/sesión
- coaches y coach preferido
- reservas del usuario
- cupones
- e-wallet
- subida de documentos

Además, `UserViewModel.logout()` llama directamente a `AuthRepositoryImpl.logout()`, saltándose la abstracción de dominio y mezclando responsabilidades de sesión con responsabilidades de usuario.

### 1.2 Decisión arquitectónica cerrada

La arquitectura objetivo se dividirá en **tres subáreas**:

1. **user/profile-account**: perfil, foto, lectura del usuario y eliminación de cuenta.
2. **user/activity-commerce**: favoritos, coach preferido, reservas, cupones y wallet.
3. **documents**: selección lógica y subida de documentos.
4. **session/auth**: logout y limpieza de sesión, fuera del área de usuario.

> Decisión cerrada: **logout no seguirá viviendo en `UserViewModel` ni en contratos de usuario**. Su hogar definitivo será la cadena de autenticación/sesión apoyada en `AuthRepository.logout()`.

---

## 2. Estructura objetivo de paquetes

Se propone evolucionar desde el paquete transversal actual hacia feature packages bajo `shared/src/commonMain/kotlin/com/humanperformcenter/shared/`.

### 2.1 Usuario

- `shared/user/domain/repository/`
- `shared/user/domain/usecase/`
- `shared/user/presentation/viewmodel/`
- `shared/user/data/repository/` *(implementaciones futuras; no forma parte de esta fase)*

### 2.2 Documentos

- `shared/documents/domain/repository/`
- `shared/documents/domain/usecase/`
- `shared/documents/presentation/viewmodel/`
- `shared/documents/data/repository/` *(implementaciones futuras; no forma parte de esta fase)*

### 2.3 Sesión

Se mantiene dentro del vertical de auth/session ya existente.

- `shared/auth/domain/usecase/` o equivalente ya vigente en auth
- `shared/auth/presentation/viewmodel/` o equivalente ya vigente en auth

> Decisión cerrada: no se creará un nuevo vertical de logout bajo `user`; se reutiliza el vertical de autenticación ya presente.

---

## 3. Interfaces objetivo en `shared/.../domain/repository/`

## 3.1 Usuario: contratos definitivos

### `shared/user/domain/repository/UserProfileRepository.kt`

Responsabilidad: lectura y edición del perfil persistido del usuario.

```kotlin
interface UserProfileRepository {
    suspend fun getUserById(id: Int): Result<User>
    suspend fun updateUser(user: User, profilePicBytes: ByteArray?): Result<User>
    suspend fun deleteProfilePic(req: DeleteProfilePicRequest): Result<Unit>
}
```

### `shared/user/domain/repository/UserAccountRepository.kt`

Responsabilidad: operaciones destructivas o administrativas sobre la cuenta.

```kotlin
interface UserAccountRepository {
    suspend fun deleteUser(email: String): Result<Unit>
}
```

### `shared/user/domain/repository/UserCoachRepository.kt`

Responsabilidad: catálogo de coaches y relación del usuario con coaches.

```kotlin
interface UserCoachRepository {
    suspend fun getCoaches(): Result<List<Professional>>
    suspend fun markFavorite(coachId: Int, serviceName: String?, userId: Int?): Result<String>
    suspend fun getPreferredCoach(customerId: Int): Result<GetPreferredCoachResponse>
}
```

### `shared/user/domain/repository/UserBookingRepository.kt`

Responsabilidad: reservas visibles/gestionables por el usuario.

```kotlin
interface UserBookingRepository {
    suspend fun getUserBookings(userId: Int): Result<List<UserBooking>>
    suspend fun cancelUserBooking(bookingId: Int): Result<Unit>
}
```

### `shared/user/domain/repository/UserCouponRepository.kt`

Responsabilidad: cupones del usuario.

```kotlin
interface UserCouponRepository {
    suspend fun addCouponToUser(userId: Int, couponCode: String): Result<Unit>
    suspend fun getUserCoupons(userId: Int): Result<List<Coupon>>
}
```

### `shared/user/domain/repository/UserWalletRepository.kt`

Responsabilidad: saldo y movimientos del e-wallet.

```kotlin
interface UserWalletRepository {
    suspend fun getEwalletBalance(userId: Int): Result<Double?>
    suspend fun getEwalletTransactions(userId: Int): Result<List<EwalletTransaction>>
}
```

### `shared/user/domain/repository/UserStatsRepository.kt`

Responsabilidad: estadísticas del usuario.

```kotlin
interface UserStatsRepository {
    suspend fun getUserStats(customerId: Int): Result<UserStatistics>
}
```

## 3.2 Documentos: contrato definitivo

### `shared/documents/domain/repository/UserDocumentRepository.kt`

Responsabilidad: subida de documentos del usuario.

```kotlin
interface UserDocumentRepository {
    suspend fun uploadDocument(userId: Int, name: String, data: ByteArray): Result<String>
}
```

## 3.3 Compatibilidad temporal

Durante la migración puede existir `UserRepository` como **facade legacy** apoyándose internamente en los contratos nuevos, pero queda decidido que:

- no se añadirán métodos nuevos a `UserRepository`
- cualquier nueva capacidad de usuario/documentos se modelará ya en uno de los contratos pequeños
- `uploadDocument(...)` deja de pertenecer conceptualmente al agregado de usuario y pasa a `UserDocumentRepository`

---

## 4. Casos de uso objetivo en `shared/.../domain/usecase/`

## 4.1 Usuario

### Perfil

- `shared/user/domain/usecase/GetUserProfileUseCase.kt`
- `shared/user/domain/usecase/UpdateUserProfileUseCase.kt`
- `shared/user/domain/usecase/DeleteUserProfilePictureUseCase.kt`

```kotlin
class GetUserProfileUseCase(private val repository: UserProfileRepository)
class UpdateUserProfileUseCase(private val repository: UserProfileRepository)
class DeleteUserProfilePictureUseCase(private val repository: UserProfileRepository)
```

### Cuenta

- `shared/user/domain/usecase/DeleteUserAccountUseCase.kt`

```kotlin
class DeleteUserAccountUseCase(private val repository: UserAccountRepository)
```

### Coaches

- `shared/user/domain/usecase/GetCoachesUseCase.kt`
- `shared/user/domain/usecase/MarkFavoriteCoachUseCase.kt`
- `shared/user/domain/usecase/GetPreferredCoachUseCase.kt`

```kotlin
class GetCoachesUseCase(private val repository: UserCoachRepository)
class MarkFavoriteCoachUseCase(private val repository: UserCoachRepository)
class GetPreferredCoachUseCase(private val repository: UserCoachRepository)
```

### Reservas

- `shared/user/domain/usecase/GetUserBookingsUseCase.kt`
- `shared/user/domain/usecase/CancelUserBookingUseCase.kt`

```kotlin
class GetUserBookingsUseCase(private val repository: UserBookingRepository)
class CancelUserBookingUseCase(private val repository: UserBookingRepository)
```

### Cupones

- `shared/user/domain/usecase/AddCouponToUserUseCase.kt`
- `shared/user/domain/usecase/GetUserCouponsUseCase.kt`

```kotlin
class AddCouponToUserUseCase(private val repository: UserCouponRepository)
class GetUserCouponsUseCase(private val repository: UserCouponRepository)
```

### Wallet

- `shared/user/domain/usecase/GetEwalletBalanceUseCase.kt`
- `shared/user/domain/usecase/GetEwalletTransactionsUseCase.kt`

```kotlin
class GetEwalletBalanceUseCase(private val repository: UserWalletRepository)
class GetEwalletTransactionsUseCase(private val repository: UserWalletRepository)
```

### Estadísticas

- `shared/user/domain/usecase/GetUserStatsUseCase.kt`

```kotlin
class GetUserStatsUseCase(private val repository: UserStatsRepository)
```

## 4.2 Documentos

- `shared/documents/domain/usecase/UploadUserDocumentUseCase.kt`

```kotlin
class UploadUserDocumentUseCase(private val repository: UserDocumentRepository)
```

## 4.3 Sesión

### Decisión cerrada para logout

No se crea `LogoutUseCase` bajo usuario. El objetivo es:

- reutilizar `AuthRepository.logout()` como contrato de dominio
- exponer un caso de uso explícito en auth/session, por ejemplo `LogoutUseCase`
- inyectarlo en un viewmodel de sesión/autenticación, no en uno de documentos ni perfil

```kotlin
class LogoutUseCase(private val authRepository: AuthRepository)
```

---

## 5. ViewModels objetivo en `shared/.../presentation/viewmodel/`

## 5.1 Usuario

### `shared/user/presentation/viewmodel/UserProfileViewModel.kt`

Responsabilidad:

- observar usuario actual en storage
- refrescar perfil remoto
- actualizar perfil
- borrar foto de perfil
- exponer `isLoading`, `userData`, `updateState`, `deleteProfilePicState`

Métodos definitivos:

- `fetchUserProfile()`
- `updateUser(candidate, profilePicBytes)`
- `clearUpdateState()`
- `deleteProfilePic(user)`
- `clearDeleteProfilePicState()`

### `shared/user/presentation/viewmodel/UserAccountViewModel.kt`

Responsabilidad:

- eliminación de cuenta
- limpieza de storage local asociada al borrado

Métodos definitivos:

- `deleteUser(email)`
- `resetDeleteState()`

### `shared/user/presentation/viewmodel/UserCoachViewModel.kt`

Responsabilidad:

- cargar coaches
- marcar favorito
- consultar coach preferido

Métodos definitivos:

- `getCoaches()`
- `markFavorite(coachId, serviceName, userId)`
- `clearMarkFavoriteState()`
- `getPreferredCoach(userId)`
- `clearGetPreferredCoachState()`

### `shared/user/presentation/viewmodel/UserBookingsViewModel.kt`

Responsabilidad:

- listar reservas
- cancelar reservas
- coordinar cancelación de notificaciones y recarga

Métodos definitivos:

- `fetchUserBookings(userId)`
- `cancelUserBooking(bookingId)`

### `shared/user/presentation/viewmodel/UserCouponsViewModel.kt`

Responsabilidad:

- gestionar formulario de cupón
- cargar cupones
- aplicar cupón

Métodos definitivos:

- `loadUserCoupon(userId)`
- `onCouponCodeChanged(code)`
- `addCouponToUser(userId, code)`

### `shared/user/presentation/viewmodel/UserWalletViewModel.kt`

Responsabilidad:

- saldo del wallet
- movimientos del wallet

Métodos definitivos:

- `loadBalance(userId)`
- `loadEwalletTransactions(userId)`

### `shared/user/presentation/viewmodel/UserStatsViewModel.kt`

Sin cambios conceptuales, pero queda alineado con `UserStatsRepository` + `GetUserStatsUseCase`.

## 5.2 Documentos

### `shared/documents/presentation/viewmodel/DocumentUploadViewModel.kt`

Responsabilidad:

- exponer `uploadState`
- subir documentos
- resetear estado de subida

Métodos definitivos:

- `uploadDocument(userId, name, data)`
- `resetUploadState()`

> Decisión cerrada: `DocumentScreen` no dependerá de `UserViewModel`; dependerá de `DocumentUploadViewModel` y de una fuente de `userId` ya resuelta por navegación o por un viewmodel de sesión/perfil.

## 5.3 Sesión

### `shared/auth/presentation/viewmodel/SessionViewModel.kt` o ampliación de `AuthViewModel`

Responsabilidad:

- `logout()`
- exponer `isLoggingOut`
- limpiar storage local al finalizar

Método definitivo:

- `logout(onSuccess)` o su variante basada en eventos de navegación

> Decisión cerrada: si se quiere minimizar piezas, `logout` puede quedar en `AuthViewModel`; lo que no es negociable es que deje de vivir en `UserViewModel` y deje de llamar directo a `AuthRepositoryImpl.logout()`.

---

## 6. Correspondencia entre pantallas y viewmodels

La siguiente tabla fija la relación objetivo de ownership entre UI y viewmodels.

| Pantalla | ViewModel objetivo | Motivo |
|---|---|---|
| `DocumentScreen` | `DocumentUploadViewModel` | La pantalla solo selecciona/sube documentos y observa `uploadState`. |
| Pantalla de editar perfil | `UserProfileViewModel` | Gestiona lectura/edición del perfil y foto. |
| Pantalla de mi perfil | `UserProfileViewModel` | Consume `userData`, `isLoading` y refresco de perfil. |
| Pantalla de configuración (acción borrar cuenta) | `UserAccountViewModel` | La eliminación de cuenta no debe vivir con edición de perfil. |
| Pantalla de configuración (acción logout) | `AuthViewModel` o `SessionViewModel` | Logout es responsabilidad de sesión, no de usuario. |
| Pantalla de favoritos / selección de coach | `UserCoachViewModel` | Contiene catálogo, favorito y preferred coach. |
| Pantalla de reservas del usuario | `UserBookingsViewModel` | Lista/cancelación de reservas y side effects de notificaciones. |
| Pantalla de cupones / checkout dependiente de cupones | `UserCouponsViewModel` | Orquesta código de cupón y listado actual. |
| Pantalla de e-wallet | `UserWalletViewModel` | Saldo y transacciones son un vertical independiente. |
| Pantalla de estadísticas | `UserStatsViewModel` | Mantiene la responsabilidad ya separada. |

### Regla operativa cerrada

Una pantalla puede observar más de un viewmodel si mezcla subdominios, pero la propiedad principal queda fijada así:

- **perfil** nunca depende del vm de documentos
- **documentos** nunca depende del vm de wallet/cupones
- **logout** nunca depende del vm de perfil/documentos

---

## 7. Dependencias Koin necesarias

## 7.1 Repositorios

```kotlin
single<UserProfileRepository> { UserProfileRepositoryImpl(get()) }
single<UserAccountRepository> { UserAccountRepositoryImpl(get()) }
single<UserCoachRepository> { UserCoachRepositoryImpl(get()) }
single<UserBookingRepository> { UserBookingRepositoryImpl(get()) }
single<UserCouponRepository> { UserCouponRepositoryImpl(get()) }
single<UserWalletRepository> { UserWalletRepositoryImpl(get()) }
single<UserStatsRepository> { UserStatsRepositoryImpl(get()) }
single<UserDocumentRepository> { UserDocumentRepositoryImpl(get()) }
```

### Decisión de implementación

En una primera etapa, estas implementaciones pueden delegar en `UserRepositoryImpl` para evitar una migración Big Bang. Aun así, el contrato de DI ya debe reflejar la separación final.

## 7.2 Casos de uso

```kotlin
singleOf(::GetUserProfileUseCase)
singleOf(::UpdateUserProfileUseCase)
singleOf(::DeleteUserProfilePictureUseCase)
singleOf(::DeleteUserAccountUseCase)
singleOf(::GetCoachesUseCase)
singleOf(::MarkFavoriteCoachUseCase)
singleOf(::GetPreferredCoachUseCase)
singleOf(::GetUserBookingsUseCase)
singleOf(::CancelUserBookingUseCase)
singleOf(::AddCouponToUserUseCase)
singleOf(::GetUserCouponsUseCase)
singleOf(::GetEwalletBalanceUseCase)
singleOf(::GetEwalletTransactionsUseCase)
singleOf(::GetUserStatsUseCase)
singleOf(::UploadUserDocumentUseCase)
singleOf(::LogoutUseCase)
```

## 7.3 ViewModels

```kotlin
viewModelOf(::UserProfileViewModel)
viewModelOf(::UserAccountViewModel)
viewModelOf(::UserCoachViewModel)
viewModelOf(::UserBookingsViewModel)
viewModelOf(::UserCouponsViewModel)
viewModelOf(::UserWalletViewModel)
viewModelOf(::UserStatsViewModel)
viewModelOf(::DocumentUploadViewModel)
viewModelOf(::AuthViewModel) // o viewModelOf(::SessionViewModel)
```

## 7.4 Dependencias auxiliares

Se mantienen donde correspondan:

- `SessionNotificationManager` -> solo en `UserBookingsViewModel`
- `SecureStorage` -> perfil/cuenta/sesión según responsabilidad
- validadores de perfil (`UserValidator`) -> solo en `UserProfileViewModel` o en un caso de uso específico de validación si se extrae después

> Decisión cerrada: `SessionNotificationManager` no debe inyectarse en documentos, cupones, wallet ni perfil.

---

## 8. Tabla de migración

## 8.1 Migración macro

| Origen | Destino |
|---|---|
| `UserRepository` | `UserProfileRepository`, `UserAccountRepository`, `UserCoachRepository`, `UserBookingRepository`, `UserCouponRepository`, `UserWalletRepository`, `UserStatsRepository`, `UserDocumentRepository` |
| `UserUseCase` | casos de uso pequeños por acción/capacidad |
| `UserViewModel` | `UserProfileViewModel`, `UserAccountViewModel`, `UserCoachViewModel`, `UserBookingsViewModel`, `UserCouponsViewModel`, `UserWalletViewModel`, `DocumentUploadViewModel`, y `AuthViewModel`/`SessionViewModel` para logout |

## 8.2 Migración método por método

| Método actual | Contrato destino | Caso de uso destino | ViewModel destino | Decisión |
|---|---|---|---|---|
| `getUserById` | `UserProfileRepository.getUserById` | `GetUserProfileUseCase` | `UserProfileViewModel` | Perfil. |
| `updateUser` | `UserProfileRepository.updateUser` | `UpdateUserProfileUseCase` | `UserProfileViewModel` | Perfil. |
| `deleteProfilePic` | `UserProfileRepository.deleteProfilePic` | `DeleteUserProfilePictureUseCase` | `UserProfileViewModel` | Perfil/foto. |
| `deleteUser` | `UserAccountRepository.deleteUser` | `DeleteUserAccountUseCase` | `UserAccountViewModel` | Cuenta. |
| `getCoaches` | `UserCoachRepository.getCoaches` | `GetCoachesUseCase` | `UserCoachViewModel` | Coaches. |
| `markFavorite` | `UserCoachRepository.markFavorite` | `MarkFavoriteCoachUseCase` | `UserCoachViewModel` | Coaches/favoritos. |
| `getPreferredCoach` | `UserCoachRepository.getPreferredCoach` | `GetPreferredCoachUseCase` | `UserCoachViewModel` | Coaches/preferred. |
| `getUserBookings` | `UserBookingRepository.getUserBookings` | `GetUserBookingsUseCase` | `UserBookingsViewModel` | Reservas. |
| `cancelUserBooking` | `UserBookingRepository.cancelUserBooking` | `CancelUserBookingUseCase` | `UserBookingsViewModel` | Reservas + notificaciones. |
| `addCouponToUser` | `UserCouponRepository.addCouponToUser` | `AddCouponToUserUseCase` | `UserCouponsViewModel` | Cupones. |
| `getUserCoupons` | `UserCouponRepository.getUserCoupons` | `GetUserCouponsUseCase` | `UserCouponsViewModel` | Cupones. |
| `uploadDocument` | `UserDocumentRepository.uploadDocument` | `UploadUserDocumentUseCase` | `DocumentUploadViewModel` | Documentos. |
| `getEwalletBalance` | `UserWalletRepository.getEwalletBalance` | `GetEwalletBalanceUseCase` | `UserWalletViewModel` | Wallet. |
| `getEwalletTransactions` | `UserWalletRepository.getEwalletTransactions` | `GetEwalletTransactionsUseCase` | `UserWalletViewModel` | Wallet. |
| `getUserStats` | `UserStatsRepository.getUserStats` | `GetUserStatsUseCase` | `UserStatsViewModel` | Estadísticas. |
| `logout` en `UserViewModel` | `AuthRepository.logout` | `LogoutUseCase` | `AuthViewModel` o `SessionViewModel` | Sale del vertical de usuario. |
| llamada directa `AuthRepositoryImpl.logout()` | **Eliminada** | se reemplaza por `LogoutUseCase` | `AuthViewModel` o `SessionViewModel` | No se permite acceso directo desde VM de usuario. |

---

## 9. Reparto de responsabilidades final y sin ambigüedad

Esta sección cierra explícitamente la discusión de ownership.

### 9.1 Lo que pertenece a perfil

Viven en perfil y en ningún otro sitio:

- `getUserById`
- `updateUser`
- `deleteProfilePic`
- observación del usuario actual desde `SecureStorage`
- validación de formulario de edición

### 9.2 Lo que pertenece a cuenta

Vive en cuenta y en ningún otro sitio:

- `deleteUser`

### 9.3 Lo que pertenece a sesión

Vive en sesión/auth y en ningún otro sitio:

- `logout`
- `isLoggingOut`
- llamada remota a `AuthRepository.logout()`
- limpieza de credenciales tras logout

### 9.4 Lo que pertenece a documentos

Vive en documentos y en ningún otro sitio:

- `uploadDocument`
- `uploadState`
- `resetUploadState`

### 9.5 Lo que pertenece a coaches

Vive en coaches y en ningún otro sitio:

- `getCoaches`
- `markFavorite`
- `getPreferredCoach`

### 9.6 Lo que pertenece a reservas

Vive en reservas y en ningún otro sitio:

- `getUserBookings`
- `cancelUserBooking`
- cancelación de notificaciones asociadas a reservas

### 9.7 Lo que pertenece a cupones

Vive en cupones y en ningún otro sitio:

- `getUserCoupons`
- `addCouponToUser`
- `onCouponCodeChanged`

### 9.8 Lo que pertenece a wallet

Vive en wallet y en ningún otro sitio:

- `getEwalletBalance`
- `getEwalletTransactions`

### 9.9 Lo que pertenece a estadísticas

Vive en estadísticas y en ningún otro sitio:

- `getUserStats`

---

## 10. Orden de migración recomendado

1. Introducir contratos pequeños y sus bindings Koin.
2. Introducir casos de uso pequeños.
3. Crear viewmodels pequeños sin borrar aún `UserViewModel`.
4. Migrar primero `DocumentScreen` a `DocumentUploadViewModel`.
5. Migrar `logout` fuera de `UserViewModel` hacia auth/session.
6. Migrar perfil, cuenta, coaches, reservas, cupones y wallet.
7. Dejar `UserViewModel` como adapter temporal o eliminarlo cuando no tenga consumidores.

---

## 11. Criterio de cierre de esta fase

La fase de diseño queda cerrada con las siguientes decisiones ya fijadas:

- `uploadDocument` pertenece al vertical de **documents**.
- `logout` pertenece al vertical de **auth/session** usando `AuthRepository.logout()` y no `AuthRepositoryImpl.logout()` directamente.
- `UserRepository`, `UserUseCase` y `UserViewModel` se consideran artefactos legacy a descomponer.
- Cada método actual tiene un contrato, caso de uso y viewmodel destino únicos.
- `DocumentScreen` deja de depender de `UserViewModel` en la arquitectura objetivo.

Con esto, ya no queda ambigüedad sobre dónde debe vivir cada método actual.

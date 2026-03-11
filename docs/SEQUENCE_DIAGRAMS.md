# Diagramas de secuencia (Mermaid)

> Se listan los flujos principales del proyecto **exceptuando** el proceso de autenticación con JWT.

## 1) Carga de perfil de usuario

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant UI as Pantalla Perfil (Compose/SwiftUI)
    participant VM as UserViewModel
    participant UC as UserUseCase
    participant REPO as UserRepositoryImpl
    participant API as API REST

    U->>UI: Abre pantalla de perfil
    UI->>VM: fetchUserProfile()
    VM->>UC: getUserById(userId)
    UC->>REPO: getUserById(userId)
    REPO->>API: GET /users/{id}
    API-->>REPO: 200 + User DTO
    REPO-->>UC: Result.success(User)
    UC-->>VM: User
    VM-->>UI: userState = Success(User)
    UI-->>U: Muestra datos del perfil
```

## 2) Actualización de perfil con foto

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant UI as Pantalla Editar Perfil
    participant VM as UserViewModel
    participant UC as UserUseCase
    participant REPO as UserRepositoryImpl
    participant API as API REST

    U->>UI: Edita datos y selecciona foto
    UI->>VM: updateUser(candidate, profilePicBytes)
    VM->>UC: updateUser(user, profilePicBytes)
    UC->>REPO: updateUser(user, profilePicBytes)
    REPO->>API: PUT/PATCH perfil + multipart foto
    API-->>REPO: 200 + User actualizado
    REPO-->>UC: Result.success(User)
    UC-->>VM: User actualizado
    VM-->>UI: updateState = Success
    UI-->>U: Confirma cambios
```

## 3) Consulta de sesiones disponibles

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant UI as Pantalla Reserva
    participant VM as DaySessionViewModel
    participant UC as DaySessionUseCase
    participant REPO as DaySessionRepositoryImpl
    participant API as API REST

    U->>UI: Selecciona producto y fecha
    UI->>VM: fetchAvailableSessions(productId, date)
    VM->>UC: getSessionsByDay(productId, date)
    UC->>REPO: getSessionsByDay(productId, date)
    REPO->>API: GET sesiones disponibles
    API-->>REPO: 200 + lista de sesiones
    REPO-->>UC: Result.success(List<Session>)
    UC-->>VM: sesiones
    VM-->>UI: dailySessionsUiState = Success
    UI-->>U: Muestra horarios disponibles
```

## 4) Reserva de sesión

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant UI as Pantalla Reserva
    participant VM as DaySessionViewModel
    participant UC as DaySessionUseCase
    participant REPO as DaySessionRepositoryImpl
    participant API as API REST

    U->>UI: Confirma reserva
    UI->>VM: makeBooking(userId, productId, serviceId, date, hour)
    VM->>UC: makeBooking(bookingRequest)
    UC->>REPO: makeBooking(bookingRequest)
    REPO->>API: POST /bookings
    API-->>REPO: 201 + booking confirmada
    REPO-->>UC: Result.success(Booking)
    UC-->>VM: Booking
    VM-->>UI: bookingEvent = Success
    UI-->>U: Muestra confirmación
```

## 5) Compra/Asignación de producto (checkout Stripe)

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant UI as Pantalla Producto/Pago
    participant SVM as StripeViewModel
    participant SUC as StripeUseCase
    participant SREPO as StripeRepositoryImpl
    participant SPVM as ServiceProductViewModel
    participant SPUC as ServiceProductUseCase
    participant SPREPO as ServiceProductRepositoryImpl
    participant API as API REST + Stripe Backend

    U->>UI: Inicia compra de producto
    UI->>SVM: startStripeCheckout(userId, productId, amount, paymentMethodId)
    SVM->>SUC: createPaymentIntent(...)
    SUC->>SREPO: createPaymentIntent(...)
    SREPO->>API: POST crear intent de pago
    API-->>SREPO: clientSecret / estado
    SREPO-->>SUC: Result.success(PaymentIntent)
    SUC-->>SVM: PaymentIntent listo
    SVM-->>UI: Checkout completado

    UI->>SPVM: assignProductToUser(userId, productId, paymentMethod, couponCode)
    SPVM->>SPUC: assignProductToUser(...)
    SPUC->>SPREPO: assignProductToUser(...)
    SPREPO->>API: POST asignar producto al usuario
    API-->>SPREPO: 200 asignación OK
    SPREPO-->>SPUC: Result.success
    SPUC-->>SPVM: éxito
    SPVM-->>UI: productsState = Success
    UI-->>U: Producto activo en cuenta
```

## 6) Carga de métodos de pago guardados

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant UI as Pantalla Métodos de pago
    participant VM as StripeViewModel
    participant UC as StripeUseCase
    participant REPO as StripeRepositoryImpl
    participant API as API REST + Stripe Backend

    U->>UI: Abre métodos de pago
    UI->>VM: loadPaymentMethods()
    VM->>UC: getUserCards(customerId)
    UC->>REPO: getUserCards(customerId)
    REPO->>API: GET tarjetas de cliente
    API-->>REPO: 200 + cards[]
    REPO-->>UC: Result.success(cards)
    UC-->>VM: cards
    VM-->>UI: estado = Success(cards)
    UI-->>U: Lista tarjetas disponibles
```

## 7) Reprogramación de una reserva

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant UI as Pantalla Mis Reservas
    participant VM as DaySessionViewModel
    participant UC as DaySessionUseCase
    participant REPO as DaySessionRepositoryImpl
    participant API as API REST

    U->>UI: Selecciona reserva y nuevo horario
    UI->>VM: modifyBookingSession(request)
    VM->>UC: modifyBookingSession(request)
    UC->>REPO: modifyBookingSession(request)
    REPO->>API: PATCH /bookings/{id}
    API-->>REPO: 200 + booking actualizada
    REPO-->>UC: Result.success(Booking)
    UC-->>VM: booking actualizada
    VM-->>UI: bookingEvent = Success
    UI-->>U: Muestra nueva fecha/hora
```

## 8) Carga del historial de reservas del usuario

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant UI as Pantalla Mis Reservas
    participant VM as UserViewModel
    participant UC as UserUseCase
    participant REPO as UserRepositoryImpl
    participant API as API REST

    U->>UI: Abre historial de reservas
    UI->>VM: fetchUserBookings(userId)
    VM->>UC: getUserBookings(userId)
    UC->>REPO: getUserBookings(userId)
    REPO->>API: GET /users/{id}/bookings
    API-->>REPO: 200 + bookings[]
    REPO-->>UC: Result.success(bookings)
    UC-->>VM: bookings
    VM-->>UI: userState = Success(bookings)
    UI-->>U: Renderiza reservas
```

## 9) Cancelación de reserva

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant UI as Pantalla Mis Reservas
    participant VM as UserViewModel
    participant UC as UserUseCase
    participant REPO as UserRepositoryImpl
    participant API as API REST

    U->>UI: Toca "Cancelar reserva"
    UI->>VM: cancelUserBooking(bookingId)
    VM->>UC: cancelUserBooking(bookingId)
    UC->>REPO: cancelUserBooking(bookingId)
    REPO->>API: DELETE /bookings/{id}
    API-->>REPO: 200/204
    REPO-->>UC: Result.success(Unit)
    UC-->>VM: éxito
    VM-->>UI: userState/updateState = Success
    UI-->>U: Confirma cancelación
```

## 10) Marcar entrenador como favorito

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant UI as Pantalla Coaches
    participant VM as UserViewModel
    participant UC as UserUseCase
    participant REPO as UserRepositoryImpl
    participant API as API REST

    U->>UI: Marca coach como favorito
    UI->>VM: markFavorite(coachId, serviceName, userId)
    VM->>UC: markFavorite(coachId, serviceName, userId)
    UC->>REPO: markFavorite(coachId, serviceName, userId)
    REPO->>API: POST /favorites
    API-->>REPO: 200 + favorito actualizado
    REPO-->>UC: Result.success
    UC-->>VM: éxito
    VM-->>UI: coachesState = Success
    UI-->>U: Coach marcado
```

## 11) Aplicar cupón a usuario

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant UI as Pantalla Cupones
    participant VM as UserViewModel
    participant UC as UserUseCase
    participant REPO as UserRepositoryImpl
    participant API as API REST

    U->>UI: Ingresa código de cupón
    UI->>VM: addCouponToUser(userId, code)
    VM->>UC: addCouponToUser(userId, couponCode)
    UC->>REPO: addCouponToUser(userId, couponCode)
    REPO->>API: POST /users/{id}/coupons
    API-->>REPO: 200 + cupón aplicado
    REPO-->>UC: Result.success(Coupon)
    UC-->>VM: cupón aplicado
    VM-->>UI: updateState = Success
    UI-->>U: Muestra descuento activo
```

## 12) Consulta de saldo y transacciones e-wallet

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant UI as Pantalla Wallet
    participant VM as UserViewModel
    participant UC as UserUseCase
    participant REPO as UserRepositoryImpl
    participant API as API REST

    U->>UI: Abre wallet
    UI->>VM: loadBalance(userId)
    VM->>UC: getEwalletBalance(userId)
    UC->>REPO: getEwalletBalance(userId)
    REPO->>API: GET /wallet/balance
    API-->>REPO: 200 + balance
    REPO-->>UC: Result.success(balance)
    UC-->>VM: balance

    UI->>VM: loadEwalletTransactions(userId)
    VM->>UC: getEwalletTransactions(userId)
    UC->>REPO: getEwalletTransactions(userId)
    REPO->>API: GET /wallet/transactions
    API-->>REPO: 200 + transactions[]
    REPO-->>UC: Result.success(transactions)
    UC-->>VM: transactions
    VM-->>UI: userState = Success(walletData)
    UI-->>U: Muestra saldo e historial
```

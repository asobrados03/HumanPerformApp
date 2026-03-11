# Diagramas de secuencia (Mermaid)

> Se listan los flujos principales del proyecto **exceptuando** el proceso de autenticación con JWT.

> Formato aplicado en todos los flujos: **Usuario -> App Móvil -> Servidor API -> Base de Datos**.

## 1) Carga de perfil de usuario

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant APP as App Móvil
    participant API as Servidor API
    participant DB as Base de Datos

    U->>APP: Abre pantalla de perfil
    APP->>API: Solicita perfil de usuario
    API->>DB: Consulta datos del usuario
    DB-->>API: Retorna perfil
    API-->>APP: Respuesta 200 + perfil
    APP-->>U: Muestra datos del perfil
```

## 2) Actualización de perfil con foto

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant APP as App Móvil
    participant API as Servidor API
    participant DB as Base de Datos

    U->>APP: Edita datos y selecciona foto
    APP->>API: Envía actualización de perfil + imagen
    API->>DB: Actualiza información del usuario
    DB-->>API: Confirmación de actualización
    API-->>APP: Respuesta 200 + usuario actualizado
    APP-->>U: Confirma cambios
```

## 3) Consulta de sesiones disponibles

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant APP as App Móvil
    participant API as Servidor API
    participant DB as Base de Datos

    U->>APP: Selecciona producto y fecha
    APP->>API: Solicita sesiones disponibles
    API->>DB: Consulta sesiones por día
    DB-->>API: Lista de sesiones
    API-->>APP: Respuesta 200 + sesiones
    APP-->>U: Muestra horarios disponibles
```

## 4) Reserva de sesión

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant APP as App Móvil
    participant API as Servidor API
    participant DB as Base de Datos

    U->>APP: Confirma reserva
    APP->>API: Envía solicitud de reserva
    API->>DB: Crea reserva
    DB-->>API: Reserva confirmada
    API-->>APP: Respuesta 201 + detalle de reserva
    APP-->>U: Muestra confirmación
```

## 5) Compra/Asignación de producto (checkout Stripe)

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant APP as App Móvil
    participant API as Servidor API
    participant DB as Base de Datos

    U->>APP: Inicia compra de producto
    APP->>API: Solicita crear pago y asignar producto
    API->>DB: Registra pago y asignación del producto
    DB-->>API: Confirmación de pago/asignación
    API-->>APP: Respuesta 200 + producto activo
    APP-->>U: Informa producto activo en cuenta
```

## 6) Carga de métodos de pago guardados

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant APP as App Móvil
    participant API as Servidor API
    participant STRIPE as Stripe

    U->>APP: Abre métodos de pago
    APP->>API: Solicita tarjetas del usuario
    API->>STRIPE: Consulta métodos de pago del cliente
    STRIPE-->>API: Retorna tarjetas guardadas
    API-->>APP: Respuesta 200 + cards[]
    APP-->>U: Lista tarjetas disponibles
```

## 7) Reprogramación de una reserva

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant APP as App Móvil
    participant API as Servidor API
    participant DB as Base de Datos

    U->>APP: Selecciona reserva y nuevo horario
    APP->>API: Solicita reprogramación
    API->>DB: Actualiza fecha/hora de reserva
    DB-->>API: Reserva actualizada
    API-->>APP: Respuesta 200 + nueva reserva
    APP-->>U: Muestra nueva fecha/hora
```

## 8) Carga del historial de reservas del usuario

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant APP as App Móvil
    participant API as Servidor API
    participant DB as Base de Datos

    U->>APP: Abre historial de reservas
    APP->>API: Solicita reservas del usuario
    API->>DB: Consulta historial de reservas
    DB-->>API: Retorna bookings[]
    API-->>APP: Respuesta 200 + historial
    APP-->>U: Renderiza reservas
```

## 9) Cancelación de reserva

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant APP as App Móvil
    participant API as Servidor API
    participant DB as Base de Datos

    U->>APP: Toca "Cancelar reserva"
    APP->>API: Solicita cancelación
    API->>DB: Elimina/anula reserva
    DB-->>API: Confirmación de cancelación
    API-->>APP: Respuesta 200/204
    APP-->>U: Confirma cancelación
```

## 10) Marcar entrenador como favorito

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant APP as App Móvil
    participant API as Servidor API
    participant DB as Base de Datos

    U->>APP: Marca coach como favorito
    APP->>API: Envía solicitud de favorito
    API->>DB: Guarda favorito del usuario
    DB-->>API: Favorito actualizado
    API-->>APP: Respuesta 200 + estado
    APP-->>U: Coach marcado
```

## 11) Aplicar cupón a usuario

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant APP as App Móvil
    participant API as Servidor API
    participant DB as Base de Datos

    U->>APP: Ingresa código de cupón
    APP->>API: Envía cupón del usuario
    API->>DB: Valida y aplica cupón
    DB-->>API: Cupón aplicado
    API-->>APP: Respuesta 200 + descuento
    APP-->>U: Muestra descuento activo
```

## 12) Consulta de saldo y transacciones e-wallet

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant APP as App Móvil
    participant API as Servidor API
    participant DB as Base de Datos

    U->>APP: Abre wallet
    APP->>API: Solicita saldo y transacciones
    API->>DB: Consulta balance y movimientos
    DB-->>API: Retorna balance + transactions[]
    API-->>APP: Respuesta 200 + walletData
    APP-->>U: Muestra saldo e historial
```

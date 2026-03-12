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
    alt Perfil encontrado
        DB-->>API: Retorna perfil
        API-->>APP: Respuesta 200 + perfil
        APP-->>U: Muestra datos del perfil
    else Perfil no encontrado / error de consulta
        DB-->>API: Error o sin registros
        API-->>APP: Respuesta 404/500 + mensaje
        APP-->>U: Muestra error y opción de reintentar
    end
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
    alt Actualización exitosa
        DB-->>API: Confirmación de actualización
        API-->>APP: Respuesta 200 + usuario actualizado
        APP-->>U: Confirma cambios
    else Error de validación/subida/BD
        DB-->>API: Error de actualización
        API-->>APP: Respuesta 400/500 + mensaje
        APP-->>U: Muestra error y conserva formulario
    end
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
    alt Sesiones disponibles
        DB-->>API: Lista de sesiones
        API-->>APP: Respuesta 200 + sesiones
        APP-->>U: Muestra horarios disponibles
    else Sin cupos / error de consulta
        DB-->>API: Lista vacía o error
        API-->>APP: Respuesta 204/500 + mensaje
        APP-->>U: Informa que no hay horarios o que ocurrió un error
    end
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
    alt Reserva creada
        DB-->>API: Reserva confirmada
        API-->>APP: Respuesta 201 + detalle de reserva
        APP-->>U: Muestra confirmación
    else Cupo ocupado / conflicto / error
        DB-->>API: Error de disponibilidad
        API-->>APP: Respuesta 409/500 + mensaje
        APP-->>U: Muestra error y sugiere otro horario
    end
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
    alt Pago y asignación exitosos
        DB-->>API: Confirmación de pago/asignación
        API-->>APP: Respuesta 200 + producto activo
        APP-->>U: Informa producto activo en cuenta
    else Pago rechazado / error de asignación
        DB-->>API: Error en transacción
        API-->>APP: Respuesta 402/500 + mensaje
        APP-->>U: Informa fallo de pago y opción de reintentar
    end
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
    alt Consulta exitosa
        STRIPE-->>API: Retorna tarjetas guardadas
        API-->>APP: Respuesta 200 + cards[]
        APP-->>U: Lista tarjetas disponibles
    else Cliente sin tarjetas / error Stripe
        STRIPE-->>API: Lista vacía o error
        API-->>APP: Respuesta 200/502 + mensaje
        APP-->>U: Muestra estado sin tarjetas o error temporal
    end
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
    alt Reprogramación exitosa
        DB-->>API: Reserva actualizada
        API-->>APP: Respuesta 200 + nueva reserva
        APP-->>U: Muestra nueva fecha/hora
    else Nuevo horario no disponible / error
        DB-->>API: Conflicto o error
        API-->>APP: Respuesta 409/500 + mensaje
        APP-->>U: Muestra error y solicita elegir otro horario
    end
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
    alt Historial encontrado
        DB-->>API: Retorna bookings[]
        API-->>APP: Respuesta 200 + historial
        APP-->>U: Renderiza reservas
    else Sin historial / error de consulta
        DB-->>API: Lista vacía o error
        API-->>APP: Respuesta 200/500 + mensaje
        APP-->>U: Muestra estado vacío o error
    end
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
    alt Cancelación exitosa
        DB-->>API: Confirmación de cancelación
        API-->>APP: Respuesta 200/204
        APP-->>U: Confirma cancelación
    else Reserva no cancelable / error
        DB-->>API: Rechazo por política o error
        API-->>APP: Respuesta 400/409/500 + mensaje
        APP-->>U: Informa que no se pudo cancelar
    end
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
    alt Favorito guardado
        DB-->>API: Favorito actualizado
        API-->>APP: Respuesta 200 + estado
        APP-->>U: Coach marcado
    else Coach inexistente / error de persistencia
        DB-->>API: Error de guardado
        API-->>APP: Respuesta 404/500 + mensaje
        APP-->>U: Muestra error al marcar favorito
    end
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
    alt Cupón válido
        DB-->>API: Cupón aplicado
        API-->>APP: Respuesta 200 + descuento
        APP-->>U: Muestra descuento activo
    else Cupón inválido/expirado o error
        DB-->>API: Cupón rechazado
        API-->>APP: Respuesta 400/404 + motivo
        APP-->>U: Informa que el cupón no aplica
    end
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
    alt Consulta exitosa
        DB-->>API: Retorna balance + transactions[]
        API-->>APP: Respuesta 200 + walletData
        APP-->>U: Muestra saldo e historial
    else Wallet no disponible / error de consulta
        DB-->>API: Error o datos incompletos
        API-->>APP: Respuesta 500 + mensaje
        APP-->>U: Muestra error temporal de wallet
    end
```

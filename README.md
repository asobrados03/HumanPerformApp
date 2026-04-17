# 🏋️ Human Perform Mobile App

> App móvil multiplataforma (Android + iOS) para la gestión integral de un centro deportivo real.
>
> Este repositorio forma parte de mi **portafolio profesional** y de mi **TFG**.
>
> ⚠️ **Importante:** la **API REST** de este proyecto está en **otro repositorio** (backend separado). Aquí se encuentra la app móvil (KMP + Android + iOS).

![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?style=for-the-badge&logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack-Compose-4285F4?style=for-the-badge&logo=jetpackcompose)
![SwiftUI](https://img.shields.io/badge/SwiftUI-0D96F6?style=for-the-badge&logo=swift&logoColor=white)
![Ktor](https://img.shields.io/badge/Ktor-087CFA?style=for-the-badge&logo=kotlin&logoColor=white)
![Koin](https://img.shields.io/badge/Koin-DI-2C7BE5?style=for-the-badge)

---


## 🎓 Alcance del TFG y repositorios

Este proyecto se divide en dos repositorios:

1. **Este repo (móvil)**: cliente multiplataforma con Kotlin Multiplatform.
2. **Repo API REST (backend)**: servicio HTTP consumido por la app.

> 🔗 Backend (añadir enlace): `https://github.com/<tu-usuario>/<tu-repo-api>`

> Si estás revisando este proyecto para selección técnica, te recomiendo evaluar ambos repos para ver el flujo end-to-end (cliente + servidor).

## 👋 Contexto para recruiters

En este proyecto trabajé sobre una base **Kotlin Multiplatform** con foco en:

- Diseño de arquitectura escalable (**Clean Architecture**).
- Implementación de flujos críticos de producto (auth, perfil, reservas, favoritos, pagos).
- Integración de clientes HTTP y manejo robusto de sesión con refresh tokens.
- Colaboración entre apps nativas (Android/iOS) compartiendo lógica en `shared`.

Si estás evaluando mi perfil para un puesto de **Mobile Engineer** (Android/KMP), este repositorio muestra cómo estructuro código mantenible y orientado a producto.

---

## 🚀 Qué resuelve la app

La app centraliza operaciones habituales de un centro deportivo:

- 🔐 **Autenticación**: login, registro, cambio/reset de contraseña y persistencia de sesión.
- 👤 **Gestión de cuenta**: perfil, foto, documentos y operaciones sobre usuario.
- 🏃 **Área deportiva**: reservas, productos/servicios y entrenador preferido.
- 💳 **Wallet/Pagos**: balance y transacciones de e-wallet (integraciones de pago presentes en Android).

---

## 🧠 Decisiones técnicas destacables

### 1) Arquitectura por capas (Clean Architecture)

Separación clara entre:

- **Domain**: entidades, casos de uso, contratos de repositorio.
- **Data**: implementaciones, data sources, DTOs, red/persistencia.
- **Presentation**: estado de UI + ViewModels + adaptación a plataforma.

Esto facilita pruebas, evolución del dominio y cambios tecnológicos sin romper la lógica de negocio.

### 2) Kotlin Multiplatform como estrategia de producto

- Lógica de negocio y networking en `shared/src/commonMain`.
- Adaptaciones nativas en `androidMain` e `iosMain`.
- UIs nativas: **Jetpack Compose** en Android y **SwiftUI** en iOS.

### 3) Seguridad y sesión

- Persistencia de tokens/usuario en almacenamiento local.
- Renovación de sesión con `refresh token` desde cliente HTTP.
- Mecanismos de limpieza de credenciales y logout.

Documentación técnica ampliada de auth: `docs/AUTH_MODULE_ARCHITECTURE.md`.

---

## 🗂️ Estructura del repositorio

```text
.
├── androidApp/    # App Android (Jetpack Compose)
├── iosApp/        # App iOS (SwiftUI)
├── shared/        # Módulo compartido KMP
│   └── src/
│       ├── commonMain/
│       ├── androidMain/
│       └── iosMain/
└── docs/          # Documentación técnica
```

Flujo funcional de alto nivel:

```text
UI Event → ViewModel → UseCase → Repository → Remote/Local DataSource
    ↑                                                      ↓
    └────────────────────── UI State Update ───────────────┘
```

---

## 🛠️ Stack tecnológico

### Core
- Kotlin Multiplatform
- Kotlin Coroutines
- Ktor Client
- Kotlinx Serialization
- Koin (DI)
- DataStore

### Android
- Jetpack Compose
- Navigation Compose
- WorkManager
- Integraciones de pago (Stripe / Google Pay)

### iOS
- SwiftUI
- Integración con framework generado desde módulo KMP

---

## 📈 Competencias demostradas

Este proyecto evidencia experiencia práctica en:

- Arquitectura de software móvil para proyectos reales.
- Diseño de código compartido entre plataformas sin sacrificar UX nativa.
- Gestión de estado y ciclo de vida con ViewModels/casos de uso.
- Consumo de APIs seguras con manejo de autenticación y refresh.
- Organización de repositorio y documentación técnica para equipos.

---

## ⚙️ Cómo ejecutar

### 0) Configurar URL de la API

Como la API vive en otro repositorio, debes levantar el backend y apuntar la app a esa URL base (entorno local o staging).


### Requisitos
- JDK 17
- Android Studio (Android)
- Xcode (iOS)

### Build del módulo compartido

```bash
./gradlew :shared:build
```

### Build Android debug

```bash
./gradlew :androidApp:assembleDebug
```

### Tests módulo compartido

```bash
./gradlew :shared:allTests
```

---

## 📚 Documentación adicional

- Arquitectura de autenticación: `docs/AUTH_MODULE_ARCHITECTURE.md`

---

## 🤝 Contacto

Si quieres conocer más sobre decisiones de arquitectura o mi contribución en el desarrollo, puedes escribirme por LinkedIn o revisar el resto de proyectos de mi portafolio.

⭐ Si te aporta valor este repositorio, una estrella ayuda mucho.

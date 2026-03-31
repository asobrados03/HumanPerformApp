## Guía de Testing — KMM + Clean Architecture

Cuando escribas tests para este proyecto, sigue estas pautas estrictamente.

### Filosofía base
- Prueba COMPORTAMIENTO, no implementación. Un test no debe romperse si se refactoriza
  el código interno sin cambiar lo que hace. Pregúntate siempre: "¿qué debe hacer
  esta unidad?" no "¿cómo lo hace?".
- Un test que conoce los detalles internos de una clase es un test frágil. Evita
  verificar que se llamó a un método interno concreto salvo que ese efecto sea
  observable desde fuera (p.ej. una llamada a la API o a la base de datos).
- Prefiere tests de la capa más alta posible que siga siendo rápido y determinista.

### Estructura de cada test
- Usa siempre el patrón AAA con comentarios explícitos: // Arrange / // Act / // Assert
- Nomenclatura: `nombreClase_condicion_resultadoEsperado`
  Ejemplo: `getUser_whenNetworkFails_returnsErrorState`
- Un test, una aserción conceptual. Si necesitas verificar varias cosas del mismo
  resultado, agrúpalas en un bloque assert cohesionado, no escribas tests separados
  que repiten el mismo Arrange/Act.

### Qué testear en cada capa

**domain/usecases**
- Testea la lógica de negocio: transformaciones, reglas, combinaciones de datos.
- Mockea los Repository por su interfaz, nunca por la implementación concreta.
- No verifiques cuántas veces se llamó al repositorio salvo que eso sea parte
  del contrato (p.ej. caché: si el dato existe, NO debe llamar a la red).
- Cubre: resultado correcto, caso vacío, propagación de errores del repositorio.

**data/repositories**
- Tests de integración: usa MockEngine de Ktor para simular la API REST real.
- El JSON de las respuestas mock debe ser idéntico al que devuelve la API real,
  incluyendo campos opcionales y nulos.
- Testea: parseo correcto, campos opcionales ausentes, errores HTTP (404, 500),
  timeout, respuesta malformada.
- NO mockees el HttpClient interno; mockea a nivel de Engine para que el
  pipeline de Ktor (serialización, headers, etc.) se ejecute de verdad.

**presentation/viewmodels**
- Testea las transiciones de UiState, no los métodos internos del ViewModel.
- Usa Turbine para consumir el StateFlow y verificar la secuencia de estados
  emitidos: Loading → Success(data) o Loading → Error(message).
- Mockea los UseCases por su interfaz o función. Si el UseCase devuelve un Flow,
  usa un fake que emita los valores que necesitas en cada test.
- No testees que el ViewModel llama a `useCase.execute()`; testea que ante
  una entrada concreta el estado final es el esperado.

### Mocks y fakes
- Usa MockK solo para dependencias externas (repositorios, servicios).
- Para colaboradores simples (p.ej. un UseCase que solo transforma datos),
  prefiere un fake escrito a mano sobre un mock: es más legible y menos frágil.
- Nunca mockees clases del dominio (entidades, value objects): úsalas reales
  con builders o constructores de test.
- Prohibido: `verify { cualquierCosa }` en tests de lógica de negocio. Solo
  en tests donde el efecto lateral ES el comportamiento observable (guardar en
  BD, enviar evento de analytics).

### Cobertura mínima esperada por componente
- UseCase: happy path + al menos 2 edge cases + propagación de error
- Repository: happy path + respuesta vacía + al menos 2 errores HTTP + error de parsing
- ViewModel: secuencia completa de estados para cada acción pública

### Ubicación de los ficheros
- Tests comunes (lógica pura): `shared/src/commonTest/kotlin/`
- Tests con MockEngine de Ktor: `shared/src/commonTest/kotlin/` (MockEngine es
  multiplataforma)
- Tests de ViewModel si usan APIs específicas de Android: `shared/src/androidUnitTest/kotlin/`

### Lo que NO debes hacer
- No uses `@VisibleForTesting` para exponer estado interno solo para tests.
- No hagas tests que solo comprueban que el mock fue llamado sin verificar el resultado.
- No dupliques la lógica de producción en el test para calcular el expected.
  El expected debe ser un valor literal conocido.
- No ignores el orden de emisiones en un Flow. Si el contrato dice Loading primero,
  verifícalo explícitamente.

## UI Tests iOS (XCUITest) con modo mock

Se añadió el target `iosAppUITests` con casos que cubren:

- `SplashView -> WelcomeView -> LoginView/RegisterView`
- navegación principal (`RootView`, tabs de `MainTabs`)
- carga de `ServicesView`, `CalendarView`, `MyProfileView`
- visualización de error de autenticación en UI

### Cómo funciona el modo mock (sin red)

Los tests lanzan la app con:

- launch argument: `-ui-testing`
- launch environment: `MOCK_NETWORK=1`

Y para escenarios concretos:

- `UI_TEST_SPLASH_LOGGED_IN=0|1` (resolución del splash sin depender de sesión real)
- `UI_TEST_FORCE_AUTH_ERROR=1` (fuerza error de login visible en pantalla)

### Ejecutar localmente (macOS + Xcode)

```bash
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 16' \
  -only-testing:iosAppUITests \
  test
```

### Ejecutar en CI

Ejemplo de paso para GitHub Actions/macOS runner:

```bash
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 16' \
  -only-testing:iosAppUITests \
  -resultBundlePath build/TestResults.xcresult \
  test
```

Recomendaciones CI:

- usar runner macOS con Xcode instalado
- fijar simulador disponible (`xcrun simctl list devices`)
- publicar `build/TestResults.xcresult` como artefacto

## Pre-release Integration Gate

Esta compuerta es **obligatoria** antes de etiquetar cualquier release (`release/*`, `hotfix/*`) o promover un build a producción.

### 1) Suites obligatorias

Para aprobar el gate deben ejecutarse y pasar las siguientes suites:

- **Shared integration suite** (`shared/src/commonTest/kotlin/integration/*`)
- **Android smoke suite** (`androidApp/src/androidTest/java/com/humanperformcenter/AppNavigationE2ETest.kt`)
- **iOS smoke suite** (`iosApp/iosAppUITests/iosAppUITests.swift`)

### 2) Criterio de aprobación

Un release candidate se aprueba solo si se cumplen **todas** estas condiciones:

- **100% pass rate** en flujos críticos de negocio (Auth, Catalog, Booking, Stripe, Profile, Documents).
- **0 tests flaky** en la ejecución de gate (no se permiten reruns para “forzar verde”).
- Ningún test en estado `ignored`, `skipped` o deshabilitado para cubrir una regresión abierta.

### 3) Comandos exactos por plataforma

> Ejecutar desde la raíz del repositorio.

**Shared (integration):**

```bash
./gradlew :shared:allTests --tests "integration.*"
```

**Android (smoke):**

```bash
./gradlew :androidApp:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.humanperformcenter.AppNavigationE2ETest
```

**iOS (smoke):**

```bash
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 16' \
  -only-testing:iosAppUITests/iosAppUITests \
  test
```

### 4) Matriz de cobertura por feature

| Feature | Shared integration | Android smoke | iOS smoke | Estado esperado gate |
|---|---|---|---|---|
| Auth | `AuthFlowIntegrationTest` | `AppNavigationE2ETest` (login path) | `iosAppUITests` (Welcome/Login) | 100% pass |
| Catalog | `CatalogAndPurchaseFlowIntegrationTest` | `AppNavigationE2ETest` (main tabs/services) | `iosAppUITests` (MainTabs/ServicesView) | 100% pass |
| Booking | `BookingFlowIntegrationTest` | `AppNavigationE2ETest` (calendar/reserva) | `iosAppUITests` (CalendarView) | 100% pass |
| Stripe | `StripeFlowIntegrationTest` | `AppNavigationE2ETest` (flujo de pago básico) | `iosAppUITests` (navegación previa a pago) | 100% pass |
| Profile | `ProfileAndDocumentsFlowIntegrationTest` (Profile scope) | `AppNavigationE2ETest` (MyProfile) | `iosAppUITests` (MyProfileView) | 100% pass |
| Documents | `ProfileAndDocumentsFlowIntegrationTest` (Documents scope) | `AppNavigationE2ETest` (acceso desde profile) | `iosAppUITests` (perfil/documentos mock) | 100% pass |

### 5) Política de bloqueo por cobertura de endpoints nuevos

- Todo PR que añada o modifique endpoints (REST/GraphQL/webhook) debe incluir:
  1. Test de integración en `shared/src/commonTest/kotlin/integration/` o test remoto/repositorio equivalente.
  2. Actualización explícita de esta matriz de cobertura si impacta un feature crítico.
- Si un endpoint nuevo no tiene cobertura de test asociada, el PR se marca con **release-blocker** y:
  - no puede mergearse a rama de release,
  - no puede entrar al corte de versión,
  - y requiere plan de cobertura aprobado en la misma PR.

## Tabla de trazabilidad feature -> test class (para PR review)

Usar esta tabla como checklist rápida para detectar huecos de cobertura al revisar PRs.

| Feature | Test classes obligatorias | Señal de hueco en PR |
|---|---|---|
| Auth | `AuthFlowIntegrationTest`, `AuthRemoteDataSourceImplTest`, `AuthRepositoryImplTest`, `AuthViewModelTest`, `AppNavigationE2ETest`, `iosAppUITests` | Cambios en login/session sin tocar al menos una prueba de integración + una de capa interna |
| Catalog | `CatalogAndPurchaseFlowIntegrationTest`, `ServiceProductRemoteDataSourceImplTest`, `ServiceProductRepositoryImplTest`, `ServiceProductViewModelTest`, `AppNavigationE2ETest`, `iosAppUITests` | Cambios en catálogo/servicios sin evidencia de cobertura en shared + smoke |
| Booking | `BookingFlowIntegrationTest`, `DaySessionRemoteDataSourceImplTest`, `UserBookingsRemoteDataSourceImplTest`, `UserBookingsRepositoryImplTest`, `DaySessionViewModelTest`, `UserBookingsViewModelTest`, `AppNavigationE2ETest`, `iosAppUITests` | Cambios en disponibilidad/reserva sin actualizar pruebas de flujo end-to-end |
| Stripe | `StripeFlowIntegrationTest`, `StripeRemoteDataSourceImplTest`, `StripeRepositoryImplTest`, `StripeViewModelTest`, `AppNavigationE2ETest`, `iosAppUITests` | Cambios en pago sin test de integración Stripe y smoke cross-platform |
| Profile | `ProfileAndDocumentsFlowIntegrationTest`, `UserProfileRemoteDataSourceImplTest`, `UserProfileRepositoryImplTest`, `UserProfileViewModelTest`, `AppNavigationE2ETest`, `iosAppUITests` | Cambios en perfil sin pruebas de persistencia/remoto + navegación |
| Documents | `ProfileAndDocumentsFlowIntegrationTest`, `UserDocumentsRemoteDataSourceImplTest`, `UserDocumentsRepositoryImplTest`, `UserDocumentsViewModelTest`, `UserDocumentSelectionViewModelTest`, `AppNavigationE2ETest`, `iosAppUITests` | Cambios en documentos sin cobertura de carga/listado/selección |

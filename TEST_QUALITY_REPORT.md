# Informe de calidad del conjunto de tests (actualizado)

Fecha de revisión: **2026-04-14**.

## Alcance revisado

- Total detectado: **61 archivos de test** con **288 casos `@Test`** (Kotlin/Multiplatform) + suites UI iOS (`XCTest`).
- Capas cubiertas: `data/remote`, `data/persistence`, `domain/usecase`, `presentation/viewmodel`, `integration`, `androidTest` E2E y `iosAppUITests`.

---

## 1) Tests E2E/UI Android

Archivos principales:
- `androidApp/src/androidTest/java/com/humanperformcenter/AppNavigationE2ETest.kt`
- `androidApp/src/androidTest/java/com/humanperformcenter/MockHttpClientProvider.kt`

✅ **Qué está bien hecho**
- Validan comportamiento observable de usuario (login, navegación y flujos clave), no solo lógica interna.
- Aíslan backend con un provider mock en el borde del sistema (buena práctica para E2E determinista).

⚠️ **Qué se puede mejorar y por qué**
- Predominan happy paths + algunos errores básicos; faltan fallos de red realistas (timeout, 500, sesión expirada).
- Dependencia fuerte de `testTag`/estructura visual; útil, pero sensible a cambios cosméticos.

🔴 **Qué es problemático**
- El enrutado de respuestas en `MockHttpClientProvider` centraliza demasiada lógica condicional y puede ocultar inconsistencias de contrato.

💡 **Sugerencia concreta**
- Migrar el mock E2E a fixtures por endpoint + matriz de estados (200/400/401/500/timeout).
- Añadir al menos 2 escenarios críticos: “token expirado redirige a login” y “error temporal muestra retry y no bloquea navegación”.

---

## 2) Tests UI iOS (XCTest)

Archivos principales:
- `iosApp/iosAppUITests/iosAppUITests.swift`
- `iosApp/iosAppUITests/iosAppUITestsLaunchTests.swift`

✅ **Qué está bien hecho**
- Existe smoke coverage de arranque y ciclo básico de UI.

⚠️ **Qué se puede mejorar y por qué**
- Cobertura funcional reducida frente a Android E2E; no hay paridad de escenarios de negocio críticos.

🔴 **Qué es problemático**
- Riesgo de regresiones no detectadas en iOS para flujos de autenticación/compra/reservas.

💡 **Sugerencia concreta**
- Replicar una “línea base” de 3 flujos críticos de Android en iOS: login válido/inválido, navegación principal, y una operación transaccional (reserva o compra).

---

## 3) Tests de integración (cross-layer)

Archivos principales:
- `shared/src/commonTest/kotlin/com/humanperformcenter/shared/integration/*IntegrationTest.kt`

✅ **Qué está bien hecho**
- Verifican colaboración real `remote -> repository -> use case` con aserciones de resultado funcional y side effects.
- Cubren dominios relevantes: auth, booking, catálogo/compra, perfil/documentos y Stripe.

⚠️ **Qué se puede mejorar y por qué**
- Profundidad limitada de caminos negativos por flujo.
- Falta una matriz sistemática de contratos fallidos (payload malformado + errores de autorización + fallos intermitentes).

🔴 **Qué es problemático**
- Si cambian las rutas de error entre capas, hoy varias regresiones podrían pasar porque predomina el escenario feliz por flujo.

💡 **Sugerencia concreta**
- Por cada flujo de integración, añadir mínimo 2 tests negativos: error HTTP tipado y payload inválido, verificando también el mapeo de error final de dominio.

---

## 4) Tests de `data/remote` y proveedor HTTP

Archivos principales:
- `shared/src/commonTest/kotlin/com/humanperformcenter/shared/data/remote/*Test.kt`
- `shared/src/commonTest/kotlin/com/humanperformcenter/shared/data/network/DefaultHttpClientProviderTest.kt`
- Fixtures en `shared/src/commonTest/resources/fixtures/**`

✅ **Qué está bien hecho**
- Excelente cobertura de contratos HTTP: método, URL, headers, content-type y parsing.
- Muy buen uso de fixtures para éxito, campos opcionales, tipos inválidos y payloads malformados.
- Buena cobertura del refresh token flow.

⚠️ **Qué se puede mejorar y por qué**
- Algunos tests validan formato textual detallado del request/body (fragilidad ante refactors sin cambio funcional).
- Existen pruebas sobre helpers de transformación interna que podrían acoplarse de más a implementación.

🔴 **Qué es problemático**
- Sobrespecificación en ciertas aserciones de serialización puede generar falsos negativos tras refactor técnico.

💡 **Sugerencia concreta**
- Priorizar aserciones semánticas (campos y contrato observable) sobre snapshots textuales de payload cuando no sean parte del contrato externo.

---

## 5) Tests de repositorios (`data/persistence`)

Archivos principales:
- `shared/src/commonTest/kotlin/com/humanperformcenter/shared/data/persistence/*Test.kt`

✅ **Qué está bien hecho**
- Buena verificación de mapeo de errores de infraestructura a dominio.
- Se comprueban side effects importantes (guardar/limpiar sesión, persistencia de usuario).

⚠️ **Qué se puede mejorar y por qué**
- Hay archivos con mayor peso en happy path que en combinatoria de errores.
- Algunos fakes son amplios y podrían ocultar duplicación de comportamiento entre tests.

🔴 **Qué es problemático**
- Riesgo de tests “espejo” de implementación cuando el fake replica demasiada lógica interna.

💡 **Sugerencia concreta**
- Estandarizar builders de fakes mínimos y una matriz reutilizable de errores transversales (401/403/500/parsing/empty body).

---

## 6) Tests de casos de uso (`domain/usecase`)

Archivos principales:
- `shared/src/commonTest/kotlin/com/humanperformcenter/shared/domain/usecase/*Test.kt`

✅ **Qué está bien hecho**
- Nombres generalmente claros y orientados a comportamiento.
- Cobertura correcta de éxito/error de negocio en la mayoría de casos.

⚠️ **Qué se puede mejorar y por qué**
- Faltan límites extremos en algunos inputs (valores vacíos, longitudes límite, combinaciones raras).

🔴 **Qué es problemático**
- En ciertos casos podría existir dependencia innecesaria de wiring/contexto cuando bastaría instanciación directa del caso de uso.

💡 **Sugerencia concreta**
- Reforzar suite con tablas de casos límite y mantener unit tests puros constructor-based cuando no se está probando DI.

---

## 7) Tests de ViewModels (`presentation`)

Archivos principales:
- `shared/src/commonTest/kotlin/com/humanperformcenter/shared/presentation/*ViewModelTest.kt`

✅ **Qué está bien hecho**
- Buen foco en estados de UI (`Loading/Success/Error`) y manejo de corrutinas/flows.
- Cobertura razonable de transiciones y mensajes de error.

⚠️ **Qué se puede mejorar y por qué**
- En varios tests se verifica secuencia exacta de emisiones; esto puede ser frágil frente a refactors internos sin impacto de UX.
- Cobertura de concurrencia aún mejorable (doble click, cancelación, carreras).

🔴 **Qué es problemático**
- La sobrespecificación temporal (orden milimétrico) aumenta mantenimiento y rompe tests por cambios internos legítimos.

💡 **Sugerencia concreta**
- Combinar aserción de “estado final + invariantes” con pocos tests de secuencia estricta solo donde el orden sea requisito funcional real.

---

## 8) Seguridad y almacenamiento de sesión

Archivo principal:
- `shared/src/commonTest/kotlin/com/humanperformcenter/shared/domain/security/AuthPreferencesTest.kt`

✅ **Qué está bien hecho**
- Sí existe cobertura para preferencias/autenticación (mejor que una ausencia total de seguridad).

⚠️ **Qué se puede mejorar y por qué**
- Cobertura parcial: falta evidencia de tests directos para componentes criptográficos de plataforma (`Crypto`, `EncryptionHandler`) y escenarios de fallo criptográfico.

🔴 **Qué es problemático**
- Riesgo de regresión en componentes sensibles de seguridad por baja cobertura específica.

💡 **Sugerencia concreta**
- Incorporar suites por plataforma para cifrado/descifrado, errores controlados y compatibilidad de formatos entre Android/iOS.

---

## Cobertura faltante relevante (transversal)

🔴 **Huecos críticos detectados**
1. Matriz incompleta de errores en integración/E2E (timeout, 5xx, token expirado, payload inconsistente).
2. Falta de paridad de pruebas UI críticas entre Android e iOS.
3. Cobertura específica insuficiente en criptografía/plataforma.

💡 **Plan recomendado (prioridad alta)**
- Sprint 1: ampliar integración con casos negativos estandarizados por flujo.
- Sprint 2: baseline iOS UI de 3 flujos críticos.
- Sprint 3: suite de seguridad crypto/storage con pruebas multiplataforma.

---

## Resumen ejecutivo

### Puntuación general de calidad: **8.1 / 10**

**Justificación breve:**
- El proyecto tiene una base de testing amplia y bien estratificada por capas, con especial fortaleza en contratos remotos y estados de ViewModel.
- La principal deuda está en cobertura negativa profunda de extremo a extremo, paridad iOS y pruebas de seguridad criptográfica.

### Top 3 problemas más graves a corregir
1. Cobertura insuficiente de errores críticos en integración/E2E.
2. Baja paridad de tests funcionales iOS frente a Android.
3. Cobertura limitada de componentes de cifrado/plataforma.

### Top 3 buenas prácticas detectadas
1. Excelente uso de fixtures y contratos HTTP en capa remota.
2. Buen nivel de tests por estados de UI en ViewModels con corrutinas/flows.
3. Presencia real de tests de integración cross-layer en dominios de negocio clave.

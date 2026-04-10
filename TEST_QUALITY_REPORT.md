# Informe de calidad del conjunto de tests

Fecha de revisión: 2026-04-10.

## Alcance revisado

- Total detectado: **59 archivos de test** con **273 casos `@Test`**.
- Capas cubiertas: `data/remote`, `data/persistence`, `domain/usecase`, `presentation/viewmodel`, `integration`, `androidTest` (E2E/UI), y ejemplos instrumentados/unitarios.

---

## 1) Tests de ejemplo / plantilla (baja relevancia funcional)

Archivos:
- `androidApp/src/test/java/com/humanperformcenter/ExampleUnitTest.kt`
- `androidApp/src/androidTest/java/com/humanperformcenter/ExampleInstrumentedTest.kt`
- `shared/src/androidDeviceTest/kotlin/com/humanperformcenter/shared/ExampleInstrumentedTest.kt`

✅ **Qué está bien**
- Son estables y rápidos.
- Verifican que el entorno de pruebas instrumentadas arranca correctamente.

⚠️ **Qué se puede mejorar**
- No aportan cobertura de negocio.
- El valor para regresión funcional real es casi nulo.

🔴 **Qué es problemático**
- Mantener tests de plantilla puede dar falsa sensación de cobertura.

💡 **Sugerencia concreta**
- Eliminarlos o moverlos a una carpeta explícita de “smoke de infraestructura”.
- Sustituir por 1–2 smoke tests reales (por ejemplo, inicialización de DI + pantalla inicial).

---

## 2) Tests E2E/UI Android (Compose + navegación)

Archivos:
- `androidApp/src/androidTest/java/com/humanperformcenter/AppNavigationE2ETest.kt`
- `androidApp/src/androidTest/java/com/humanperformcenter/MockHttpClientProvider.kt`

✅ **Qué está bien**
- Verifican comportamientos observables end-to-end: login inválido/válido, navegación por tabs y flujo de compra/reserva.
- Uso sistemático de `testTag` y helpers (`waitAndClick`, `waitUntilVisible`) que mejora robustez frente a asincronía.
- Uso de doble de red para evitar dependencia de backend real y mejorar determinismo.

⚠️ **Qué se puede mejorar**
- Cobertura parcial de errores: se prueba credencial inválida, pero no timeout, 500, payloads incompletos o sesión expirada.
- Falta verificar estados de carga/disabled de CTAs durante requests.
- No se valida accesibilidad básica (semántica/labels) ni persistencia de estado al rotar/recrear actividad.

🔴 **Qué es problemático**
- `MockHttpClientProvider` concentra lógica de routing por path en un gran `when`; si el contrato crece, será difícil detectar inconsistencias finas.
- El flujo E2E depende de tags exactos; un refactor visual menor puede romper tests sin cambio funcional (acoplamiento moderado a estructura de UI).

💡 **Sugerencia concreta**
- Separar fixtures por endpoint (JSON de éxito/error) y construir un mini DSL de respuestas.
- Añadir tests E2E de “sesión expirada redirige a login” y “error de red muestra retry”.
- Preferir assertions centradas en resultado de usuario (mensaje/estado final) en lugar de secuencias largas de clicks cuando sea posible.

---

## 3) Tests de integración (flujo entre capas)

Archivos:
- `shared/src/commonTest/kotlin/com/humanperformcenter/shared/integration/*.kt`

✅ **Qué está bien**
- Verifican colaboración real entre `remote -> repository -> use case`.
- Cubren flujos críticos de negocio: autenticación, booking, catálogo/compra, perfil/documentos, stripe.
- Contratos de entrada/salida razonablemente explícitos (payloads de endpoints y asserts de resultados).

⚠️ **Qué se puede mejorar**
- Cada archivo tiene solo un test principal; profundidad limitada para errores y edge cases.
- Se mockea red completa (correcto en frontera), pero faltan escenarios de fallo encadenado (ej. éxito parcial + rollback esperado).

🔴 **Qué es problemático**
- Al existir un único escenario “feliz” por flujo, regresiones en rutas negativas pueden pasar inadvertidas.

💡 **Sugerencia concreta**
- Duplicar cada flujo con al menos 2 variantes negativas: error HTTP tipado y payload malformado.
- Añadir validación explícita de side effects persistentes (no solo `isSuccess`).

---

## 4) Tests `data/remote` (clientes HTTP / parsing)

Archivos:
- `shared/src/commonTest/kotlin/com/humanperformcenter/shared/data/remote/*Test.kt`
- `shared/src/commonTest/kotlin/com/humanperformcenter/shared/data/network/DefaultHttpClientProviderTest.kt`
- Fixtures en `shared/src/commonTest/resources/fixtures/**`

✅ **Qué está bien**
- Muy buena cobertura de contratos HTTP: método, URL, cabeceras, content-type y deserialización.
- Uso intenso de fixtures para variantes de éxito, optional missing, wrong type, malformed y errores estándar.
- Buena cobertura del refresh token flow en `DefaultHttpClientProviderTest`.

⚠️ **Qué se puede mejorar**
- En algunos casos hay asserts sobre detalles de construcción del body multipart (regex/string) que pueden volver frágiles los tests ante refactors internos del serializado.
- Faltan verificaciones de idempotencia/reintentos cuando aplique.

🔴 **Qué es problemático**
- Algunos tests prueban helpers/transformaciones internas de implementación (ej. conversión de fecha “legacy”), elevando acoplamiento a detalle interno.

💡 **Sugerencia concreta**
- Mantener tests de contrato en términos de campos semánticos esperados y reducir dependencia al formato textual exacto del multipart.
- Mover validaciones de helper interno a tests unitarios dedicados de utilidades públicas, o cubrirlo indirectamente por contrato externo.

---

## 5) Tests `data/persistence` (repositorios)

Archivos:
- `shared/src/commonTest/kotlin/com/humanperformcenter/shared/data/persistence/*Test.kt`

✅ **Qué está bien**
- Verifican mapeo de errores de infraestructura a errores de dominio (ej. `AuthDomainError`).
- Verifican side effects relevantes (guardar/limpiar tokens, persistir usuario).
- Nombres de tests mayoritariamente claros en estilo `when_X_then_Y`.

⚠️ **Qué se puede mejorar**
- En algunos repositorios, el set de casos negativos es más corto que el de éxito.
- Dependencia de Fakes “todo en uno” por archivo puede ocultar duplicación.

🔴 **Qué es problemático**
- Riesgo de “espejar implementación” cuando el fake reproduce demasiado la lógica esperada.

💡 **Sugerencia concreta**
- Introducir builders de fakes reutilizables por módulo y matriz de casos negativos comunes (401/403/500/parsing).

---

## 6) Tests de `domain/usecase`

Archivos:
- `shared/src/commonTest/kotlin/com/humanperformcenter/shared/domain/usecase/*Test.kt`

✅ **Qué está bien**
- Cobertura sólida del happy path y errores esperables por validación de negocio.
- Buen foco en comportamiento observable de cada caso de uso (Result success/failure + tipo de error).

⚠️ **Qué se puede mejorar**
- Varios tests dependen de Koin para construir use cases simples; aumenta complejidad del setup sin necesidad en unit tests puros.
- Faltan algunos límites de entrada (strings vacíos/extremos/nullables transformados).

🔴 **Qué es problemático**
- Acoplamiento moderado al wiring DI en pruebas que podrían ser constructor-based y más rápidas.

💡 **Sugerencia concreta**
- En unit tests de use case: instanciación directa + fakes pequeños.
- Dejar pruebas de DI para tests de integración del módulo de inyección.

---

## 7) Tests de `presentation/viewmodel`

Archivos:
- `shared/src/commonTest/kotlin/com/humanperformcenter/shared/presentation/*Test.kt`

✅ **Qué está bien**
- Buena cobertura de estados UI (`Idle/Loading/Success/Error`) y transiciones con coroutines.
- Uso correcto de `StandardTestDispatcher` y `turbine` en varios casos para observar flujos.
- Casos de fallback de mensajes y reset de estado están bien cubiertos.

⚠️ **Qué se puede mejorar**
- En ciertos tests se verifican secuencias exactas de emisión; eso puede ser frágil si cambia internamente la estrategia de emisión sin cambiar UX final.
- Falta cobertura de concurrencia (acciones dobles rápidas, cancelación, reintentos simultáneos).

🔴 **Qué es problemático**
- Algunos asserts de “call count exacto” o pasos intermedios pueden sobrespecificar implementación.

💡 **Sugerencia concreta**
- Complementar con asserts de estado final observable + invariantes, reduciendo dependencia en el orden fino cuando no es requisito funcional.
- Añadir tests de debouncing/click repetido para acciones críticas.

---

## Cobertura faltante relevante (visión transversal)

🔴 **Huecos críticos detectados**
1. **Módulos sin evidencia clara de tests directos** en áreas sensibles como seguridad/cifrado (`Crypto`, `EncryptionHandler`, `AuthPreferences`) y parte de validadores de dominio.
2. **Escenarios no funcionales** casi ausentes: rendimiento básico, resiliencia a latencia alta, cancelación y timeouts de usuario.
3. **Matriz de errores E2E** limitada: solo un subconjunto de fallos reales de backend/UI.

💡 **Plan de cierre recomendado (prioridad alta)**
- Añadir suite dedicada de seguridad/crypto con casos deterministas y de fallo controlado.
- Añadir suite de resiliencia en ViewModels (retry/cancel/race conditions).
- Expandir integración/E2E con casos de expiración de sesión, 5xx y payload inválido.

---

## Resumen ejecutivo

### Puntuación general: **7.8 / 10**

Justificación:
- **Fortalezas**: volumen de pruebas alto, buena segmentación por capas, buen uso de fixtures y cobertura de contratos HTTP/estados UI.
- **Debilidades**: algunos tests con acoplamiento a implementación (secuencias exactas, formato interno), huecos en seguridad y pruebas negativas E2E/integración.

### Top 3 problemas más graves
1. **Cobertura insuficiente de rutas negativas en integración/E2E** (alto riesgo de regresión en producción).
2. **Acoplamiento a detalles internos en parte de tests remotos/UI** (fragilidad ante refactors).
3. **Falta de foco explícito en componentes de seguridad/crypto**.

### Top 3 buenas prácticas detectadas
1. **Uso consistente de fixtures** para casos de parsing y errores de API.
2. **Cobertura de estados de ViewModel** con herramientas correctas de testing de coroutines/flows.
3. **Presencia de pruebas de integración cross-layer** (no solo unit tests aislados).


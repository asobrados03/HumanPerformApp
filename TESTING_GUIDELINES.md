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

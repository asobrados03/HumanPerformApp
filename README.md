# Human Perform 2025

App Movil para Human Perform

## Arquitectura de la aplicación móvil
-	Patrones y tecnologías: Se ha aplicado Clean Architecture junto con Hexagonal Architecture, usando Kotlin Multiplatform para compartir la lógica de negocio en commonMain entre Android e iOS.
-	Capas del sistema:
  1.	Dominio (domain/): Contiene entidades, repositorios (interfaces) y casos de uso, aislados de detalles técnicos.
  2.	Infraestructura (data/): Implementa esas interfaces con modelos de datos, clientes Ktor para red y repositorios que lanzan peticiones a la API REST usando los clientes HTTP y DataStore para persistencia local.
  3.	Presentación (UI y View Models): (implícita en la descripción) Genera eventos de usuario que disparan casos de uso.
-	Puertos y adaptadores: El dominio solo conoce interfaces; las adaptaciones (red, BD, UI) se inyectan mediante dependencias, lo que mejora testabilidad y flexibilidad.
-	Flujo unidireccional de datos:
  1.	El usuario interactúa en la UI →
  2.	Se dispara un caso de uso →
  3.	Éste valida, llama a repositorios a través de interfaces →
  4.	Repositorios gestionan red y/o base de datos, convierten DTO ⇄ entidades →
  5.	El resultado vuelve al caso de uso y a la UI.
-	Soporte multiplataforma: Con expect/actual (en Platform.kt), se definen contratos comunes en commonMain y se implementan detalles nativos en androidMain e iosMain.
Esta estructura garantiza separación de responsabilidades, facilita las pruebas, la evolución del sistema y el aprovechamiento de capacidades nativas en cada plataforma manteniendo una sola base de código de negocio.

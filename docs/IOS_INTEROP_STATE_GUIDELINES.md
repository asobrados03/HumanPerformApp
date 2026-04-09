# Guía de interoperabilidad iOS ↔ Kotlin para estado en ViewModels compartidos

## ¿Por qué no usar `@NativeCoroutinesState` en **todos** los `StateFlow`?

`@NativeCoroutinesState` funciona bien cuando el estado que cruza a Swift es simple (tipos primitivos, data classes planas o estados fáciles de mapear).

En este proyecto hay varios `UiState` modelados como jerarquías (`sealed class`) y colecciones complejas. En Swift, esos tipos suelen exponerse con nombres generados y castings que son frágiles y verbosos.

Por eso, en algunos ViewModels se priorizan helpers de lectura (`stateKind`, `stateMessage`, listas normalizadas, etc.) para dar una API Swift estable y legible.

## ¿Por qué no sincronizar todo con `StateFlow` compartido entre Kotlin y Swift?

No todo estado necesita sincronización bidireccional entre plataformas. Hay casos donde:

- El estado es **transitorio de UI** (alertas, pantallas activas, foco, navegación local).
- El estado compartido usa tipos que en Swift exigen muchos casts o bridges.
- Se necesita minimizar acoplamiento entre UI iOS y detalle interno del `UiState` Kotlin.

En esos escenarios, helpers pequeños en Kotlin reducen complejidad y evitan que iOS dependa de detalles de implementación de clases generadas.

## Criterio recomendado

Usar `@NativeCoroutinesState` por defecto cuando:

1. El estado es estable y consumible fácilmente desde Swift.
2. El costo de observación/reactividad en iOS es menor que mantener helpers.
3. No obliga a casts complejos ni a depender de nombres generados frágiles.

Usar helpers cuando:

1. El `UiState` es complejo o con jerarquías que degradan la DX en Swift.
2. Solo se necesita exponer una vista simplificada del estado.
3. Queremos desacoplar iOS de la forma interna del estado Kotlin.

## Refactor futuro sugerido

Para aumentar uso de `@NativeCoroutinesState` sin perder ergonomía iOS:

- Introducir DTOs/`UiState` "Swift-friendly" (planos, con enums simples y payloads claros).
- Mantener en Kotlin la lógica de transformación de `sealed class` → modelo de consumo iOS.
- Estandarizar una convención por ViewModel: `stateFlow principal` + `helpers mínimos` solo donde aporte valor.

# ADR 0001: Sincronización del catálogo con arquitectura hexagonal

Fecha: 2026-10-16

## Contexto

El servicio de catálogo se generó con JHipster, que organiza el código en capas
(`domain`, `repository`, `service`, `web`). Esa estructura sirve para el CRUD de
categorías, profesionales y horarios.

La sincronización con la cátedra es distinta: depende de sistemas externos (API REST
ahora; Redis y Kafka en el próximo sprint) y tiene reglas propias, como no guardar un
snapshot con referencias rotas y no avanzar la versión si algo falla.

## Decisión

La sincronización vive en el paquete `sync` con arquitectura hexagonal:

- `domain`: el snapshot y sus reglas, sin Spring, JPA ni Jackson.
- `ports`: interfaces de entrada (`SynchronizeCatalogUseCase`) y de salida
  (`CatalogSnapshotSource`, `CatalogReplica`).
- `application`: el caso de uso, que solo conoce los puertos.
- `infrastructure`: los adaptadores (REST de la cátedra, persistencia JPA, arranque)
  y la configuración de Spring.

El código generado por JHipster queda en capas, como lo genera.

Las reglas se verifican en cada build con ArchUnit (`SyncArchitectureTest`), y
`TechnicalStructureTest` de JHipster excluye el paquete `sync`.

## Consecuencias

- El caso de uso se prueba sin base ni red, con implementaciones falsas de los puertos.
- Para el sprint 2 alcanza con agregar adaptadores (Redis y Kafka) sin tocar el dominio.
- Conviven dos estilos en el mismo servicio, separados por paquete.
- Hay más clases que si se hiciera todo en un único servicio de Spring.

## Alternativas descartadas

- **Todo en capas, como JHipster:** la sincronización quedaría acoplada a Spring y a
  la API de la cátedra, y probarla requeriría levantar todo.
- **Reescribir todo el servicio en hexagonal:** mucho trabajo sobre código generado
  que ya funciona, sin beneficio para el CRUD.

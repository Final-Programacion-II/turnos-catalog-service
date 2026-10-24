# Sincronización del catálogo

El servicio de catálogo tiene una copia propia de las categorías, profesionales y horarios de la cátedra. Las búsquedas se hacen siempre sobre esa copia, no contra el servidor de la cátedra.

Para mantenerla al día se usan tres cosas:

- **REST (snapshot):** trae el catálogo completo. Se usa cuando la base está vacía o cuando nos atrasamos demasiado.
- **Redis:** tiene la versión actual del catálogo y los cambios de cada versión. Se usa para actualizar de a poco.
- **Kafka:** solo avisa que hay una versión nueva (`CatalogUpdated`). No trae datos.

Como Kafka solo avisa, si se pierde un mensaje no se pierden datos. Basta con volver a comparar versiones.

## Cómo decide qué hacer

Cada vez que se sincroniza, se compara la versión local con la que dice Redis:

```mermaid
flowchart TD
    A[Sincronizar] --> B{¿Tengo versión local?}
    B -- No --> S[Pedir snapshot]
    B -- Sí --> C{¿Estoy al día?}
    C -- Sí --> OK[No hacer nada]
    C -- No --> D{¿Redis todavía tiene los cambios que me faltan?}
    D -- Sí --> F[Aplicar los cambios uno por uno]
    D -- No --> S
```

"¿Redis todavía tiene los cambios?" se responde así: la versión local tiene que ser mayor o igual a `oldest-available-version`. Por ejemplo, si estoy en la 3 y Redis solo guarda desde la 4, me falta un cambio y pido el snapshot (caso 18.2 de la referencia).

## Snapshot

Se usa en el primer arranque o cuando quedamos muy atrasados.

1. Se pide `GET /api/synchronization/snapshot`.
2. Se revisa que todo esté bien relacionado: cada profesional con una categoría que existe y cada horario con un profesional que existe.
3. Se guarda todo en **una sola transacción**. Si algo falla, no queda nada a medias.
4. Lo que teníamos y no vino en el snapshot no se borra: se marca `enabled = false`.
5. Se guarda la versión del snapshot como versión local.

## Cambios incrementales

```mermaid
sequenceDiagram
    participant K as Kafka
    participant C as Catálogo
    participant R as Redis
    participant DB as Base local

    K-->>C: CatalogUpdated (eventId)
    C->>DB: ¿ya procesé este eventId?
    alt ya procesado
        Note over C: se ignora
    else nuevo
        C->>R: versión actual
        loop por cada versión que falta
            C->>R: cambios de esa versión
            C->>DB: aplicar y avanzar la versión
        end
        C->>DB: guardar eventId
    end
```

Algunas decisiones:

- Cada versión se aplica en su propia transacción. Si la 6 sale bien y la 7 falla, la base queda bien en la 6 y la próxima vez sigue desde ahí.
- Las versiones se aplican en orden. Si se intenta aplicar una que no sigue a la actual, se corta con un error.
- El listener no usa el número de versión que trae el mensaje: siempre compara contra Redis. Así un mensaje viejo no puede hacer retroceder la copia.
- Los cambios en Redis traen solo IDs. Los datos de cada entidad se leen de su hash (`professionals`, etc.).

## Mensajes repetidos, perdidos o desordenados

Kafka puede entregar el mismo mensaje más de una vez. Por eso:

- Cada `eventId` procesado se guarda en la tabla `processed_catalog_event`. Si llega de nuevo, se ignora.
- Además, si una versión ya está aplicada, aplicarla otra vez no cambia nada.

| Qué pasa | Cómo se resuelve |
| --- | --- |
| Llega un mensaje repetido | Se ignora por el `eventId` |
| Llega un aviso viejo | Ya estoy al día, no hace nada |
| Se pierde un aviso | El próximo aviso aplica todas las versiones que falten |
| Me atrasé demasiado | Se pide el snapshot |
| La app estuvo apagada | Sincroniza sola al arrancar |
| Falla Redis, la API o la base | Queda en el log y la versión no avanza |

**Pendiente:** si la sincronización falla al procesar un aviso, ese aviso no se reintenta. El atraso se recupera con el siguiente aviso o al reiniciar. Para no depender de eso, voy a agregar una sincronización periódica y un endpoint que muestre el estado y el último error.

## Dónde está en el código

Todo está en el paquete `sync` (ver [ADR 0001](adr/0001-sincronizacion-hexagonal.md)):

- `CatalogSyncService`: decide qué hacer (snapshot, cambios o nada).
- `CatedraRestAdapter`: pide el snapshot.
- `RedisCatalogChangeSource`: lee versiones y cambios de Redis.
- `JpaCatalogReplica`: guarda en PostgreSQL.
- `CatalogUpdatedListener`: escucha Kafka.
- `CatalogSyncOnStartup`: sincroniza al arrancar.

La versión local está en la tabla `catalog_sync_state`.

## Configuración y pruebas

Las URLs y credenciales van en variables de entorno (ver `.env.example`). El `.env` real no se sube.

Para correr los tests:

```bash
./mvnw verify
```

## Prueba contra la cátedra (23/10)

- Con la base vacía cargó el snapshot: versión 1, con 8 categorías, 24 profesionales y 44 horarios.
- Al reiniciar leyó la versión de Redis y vio que ya estaba al día.
- Kafka quedó suscripto al topic asignado.

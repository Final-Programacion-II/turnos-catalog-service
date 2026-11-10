# API del catálogo

El catálogo expone una API REST de **solo lectura**. Todo sale de la copia local: ninguna
consulta va al servidor de la cátedra. Quien la usa es el servicio de turnos (y, a través de
él, la app).

## Convenciones

- Base: `http://localhost:8081` en desarrollo; `http://catalog:8081` dentro del Docker Compose.
- Todos los endpoints de `/api/**` piden un JWT de usuario en `Authorization: Bearer <token>`.
  Los tokens los emite el servicio de turnos al hacer login; el catálogo solo los valida (firma
  y vencimiento) con el secreto compartido. Sin token, o con uno vencido o mal firmado: **401**.
- Las respuestas son JSON. Las horas van como `HH:mm:ss` y los días con el enum `Weekday`:
  `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY`.
- Las fechas de auditoría (`createdAt`, `updatedAt`, `lastSyncedAt`...) van en UTC, formato ISO-8601.
- Cada objeto tiene dos ids: `id` es el de la base local del catálogo y `externalId` es el de la
  cátedra. Para hablar con la cátedra (reservar, por ejemplo) se usa `externalId`.
- No hay `POST`, `PUT`, `PATCH` ni `DELETE`. El catálogo se modifica en la cátedra y llega por la
  sincronización ([sincronizacion.md](sincronizacion.md)).

## Profesionales

### Listar todos

```http
GET /api/professionals
```

Devuelve todos los profesionales, habilitados o no, con su categoría. Ejemplo de un elemento:

```json
{
  "id": 1067,
  "externalId": 218,
  "firstName": "Romina",
  "lastName": "Acosta",
  "enabled": true,
  "createdAt": "2026-10-23T14:02:11Z",
  "updatedAt": "2026-11-08T21:00:03Z",
  "category": {
    "id": 1003,
    "externalId": 4,
    "name": "Ginecologia",
    "description": "...",
    "enabled": true,
    "createdAt": "2026-10-23T14:02:11Z",
    "updatedAt": "2026-10-23T14:02:11Z"
  }
}
```

### Buscar

```http
GET /api/professionals/search?categoryId=&name=&enabled=&day=&from=&to=
```

Todos los filtros son opcionales y se combinan con "y". Sin filtros devuelve todos. El
resultado viene ordenado por apellido y nombre.

| Filtro | Ejemplo | Qué hace |
| --- | --- | --- |
| `categoryId` | `1003` | Solo los de esa categoría (id local). |
| `name` | `romi aco` | Busca en "nombre apellido", sin importar mayúsculas. Sirve con una parte del nombre, del apellido o de los dos. |
| `enabled` | `true` | `true`: el profesional **y** su categoría están habilitados (se puede reservar). `false`: alguno de los dos está deshabilitado. |
| `day` | `TUESDAY` | Solo los que tienen algún horario habilitado ese día. |
| `from` | `09:00` | Solo los que tienen algún horario habilitado que termina después de esa hora. |
| `to` | `12:00` | Solo los que tienen algún horario habilitado que empieza antes de esa hora. |

`day`, `from` y `to` se aplican sobre **el mismo horario**: `day=TUESDAY&from=09:00&to=12:00`
devuelve a quien atiende el martes en algún momento entre las 9 y las 12 (alcanza con que el
horario se superponga con esa franja, no tiene que cubrirla entera).

Errores:

- `from` igual o posterior a `to` → **400** con `message: "error.invalidtimerange"`.
- Un valor con formato inválido (`day=LUNES`, `from=9hs`, `categoryId=abc`) → **400**.

Ejemplos:

```bash
# Quiénes atienden en Clínica Médica los lunes a la mañana y se pueden reservar
curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8081/api/professionals/search?categoryId=1001&day=MONDAY&from=08:00&to=12:00&enabled=true"

# Buscar por nombre
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8081/api/professionals/search?name=acosta"
```

### Uno por id

```http
GET /api/professionals/{id}
```

**200** con el profesional (mismo formato que en la lista) o **404** si no existe en la copia local.

### Agenda

```http
GET /api/professionals/{id}/agenda
```

Es lo que usa turnos para calcular la disponibilidad y para reservar (ver el contrato más abajo).
**200** con la agenda o **404** si el profesional no existe en la copia local.

## Categorías

```http
GET /api/professional-categories
GET /api/professional-categories/{id}
```

Devuelven todas las categorías (habilitadas o no) o una por id (**404** si no existe). Cada una
tiene `id`, `externalId`, `name`, `description`, `enabled`, `createdAt` y `updatedAt`.

## Horarios semanales

```http
GET /api/weekly-schedules
GET /api/weekly-schedules/{id}
```

Devuelven todos los horarios (habilitados o no) o uno por id (**404** si no existe):

```json
{
  "id": 1152,
  "externalId": 332,
  "dayOfWeek": "TUESDAY",
  "startTime": "13:00:00",
  "endTime": "16:00:00",
  "slotDurationMinutes": 30,
  "enabled": true,
  "createdAt": "2026-10-23T14:02:11Z",
  "updatedAt": "2026-10-23T14:02:11Z",
  "professional": { "id": 1067, "...": "..." }
}
```

Un horario dice "los martes de 13 a 16, turnos de 30 minutos". Los turnos concretos de una fecha
no los calcula el catálogo: los calcula turnos con la agenda y las ocupaciones de la cátedra.

## Estado de la sincronización

```http
GET /api/catalog/sync-status
```

Sirve para saber si la copia local está al día y con qué versión de la cátedra:

```json
{
  "status": "OK",
  "catalogVersion": 10,
  "lastSyncedAt": "2026-11-09T12:00:04Z",
  "lastAttemptAt": "2026-11-09T12:05:04Z",
  "lastError": null
}
```

| `status` | Significa |
| --- | --- |
| `NOT_SYNCED` | Todavía no se sincronizó nunca (base recién creada). |
| `OK` | El último intento salió bien. `catalogVersion` es la versión de la copia. |
| `ERROR` | El último intento falló. `lastError` dice por qué. La copia anterior queda intacta y se sigue usando. |

## Cuenta

```http
GET /api/authenticate   → 204 si el token es válido, 401 si no
GET /api/account        → login y roles que vienen en el token
```

Son los endpoints estándar de JHipster para comprobar el token. El catálogo no tiene
`POST /api/authenticate`: no hace login ni emite tokens
([decisiones.md](decisiones.md), punto 8).

## Contrato con turnos

Es la única llamada entre los dos servicios. Turnos la hace antes de calcular la
disponibilidad y antes de reservar, reenviando el JWT del usuario que hizo el pedido:

```http
GET http://catalog:8081/api/professionals/{id}/agenda
Authorization: Bearer <jwt del usuario>
```

Respuesta 200:

```json
{
  "id": 1067,
  "externalId": 218,
  "firstName": "Romina",
  "lastName": "Acosta",
  "categoryName": "Ginecologia",
  "bookable": true,
  "catalogVersion": 10,
  "schedules": [
    { "externalId": 332, "dayOfWeek": "TUESDAY", "startTime": "13:00:00", "endTime": "16:00:00", "slotDurationMinutes": 30 }
  ]
}
```

Qué garantiza el catálogo en esta respuesta:

- `bookable` es `true` solo si el profesional **y** su categoría están habilitados.
- `schedules` trae **solo los horarios habilitados**, ordenados por día y hora de inicio.
- `catalogVersion` es la versión de la copia local con la que se respondió. Si es `null`, el
  catálogo todavía no se sincronizó nunca.
- `externalId` del profesional es el que turnos manda a la cátedra para reservar.

Si cambia el formato de esta respuesta hay que cambiar también `CatalogRestAdapter` en turnos.
Del lado de turnos, un 404 se convierte en `PROFESSIONAL_NOT_FOUND` y cualquier otro error o
timeout en `CATALOG_UNAVAILABLE` (ver la API de turnos).

## Errores

Los errores son `application/problem+json` (formato estándar de JHipster). Por ejemplo, para
un rango de horas inválido la respuesta trae, entre otros campos:

```json
{
  "title": "La hora desde tiene que ser anterior a la hora hasta",
  "status": 400,
  "path": "/api/professionals/search",
  "message": "error.invalidtimerange",
  "params": "professional"
}
```

En los demás errores `message` es `error.http.<código>`, por ejemplo `error.http.404`.

| Código | Cuándo |
| --- | --- |
| 400 | Un filtro con formato inválido, o `from` no es anterior a `to`. |
| 401 | Falta el token, está vencido o la firma no coincide. Viene sin cuerpo, solo con el encabezado `WWW-Authenticate`. |
| 404 | El id no existe en la copia local. |
| 405 | Método no soportado (por ejemplo `POST` sobre un recurso del catálogo). |

Quien llame tiene que decidir por `status` y `message`, no por el texto de `title` o `detail`.

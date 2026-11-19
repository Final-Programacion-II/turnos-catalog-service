# Decisiones de diseño

Registro de las decisiones técnicas del proyecto, con el motivo y las alternativas descartadas.

## 1. JHipster como base de los servicios

Los dos servicios se generan con JHipster como aplicaciones `monolith` sin cliente
(`skipClient`), con autenticación JWT.

- Motivo: resuelve seguridad JWT, migraciones con Liquibase y configuración de tests,
  y deja el foco en la sincronización y el flujo de reservas.
- Descartado: el tipo `microservice` de JHipster, porque requiere gateway y registry
  (Consul), que el sistema no necesita y agregan infraestructura sin aportar al objetivo.

## 2. PostgreSQL, una instancia por servicio

Cada servicio tiene su propio contenedor de PostgreSQL y sus propias migraciones.
Ningún servicio accede a la base del otro.

- Motivo: la separación de datos queda garantizada por la infraestructura y no solo
  por convención.
- Descartado: dos esquemas en un mismo servidor. Cumple el requisito, pero la
  separación depende de la disciplina y no de la arquitectura.

## 3. Los usuarios pertenecen al servicio de turnos

El registro y el login del usuario final viven en `turnos-appointments-service`.
El `externalPatientId` que se envía a la cátedra es el id interno y estable del usuario.
El servicio de catálogo no guarda usuarios: valida el JWT con el mismo secreto.

- Motivo: la propiedad de las reservas se controla en turnos, donde está la identidad
  del usuario.
- Descartado: un servicio de autenticación separado, porque agrega un tercer servicio
  que el enunciado no pide.

## 4. Turnos consulta al catálogo propagando el JWT del usuario

Las consultas de disponibilidad siempre las inicia un usuario autenticado, por lo que
turnos reenvía su token al catálogo.

- Motivo: es simple, no requiere credenciales adicionales y cada llamada queda asociada
  a un usuario.
- Descartado: un token técnico interno entre servicios. Se reconsidera si aparece
  alguna llamada que no dependa de un usuario.
- Cómo se valida: los dos servicios firman y validan los JWT con el mismo secreto
  (HMAC), configurado con la variable `JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64SECRET`.
  El catálogo verifica firma y vencimiento del token que le llega, igual que si lo
  hubiera emitido él. Si el secreto no coincide, responde 401.
- Limitación: con un secreto compartido, cualquiera de los dos servicios puede emitir
  tokens válidos para el otro. Por eso el catálogo no emite tokens: se sacó su login de
  ejemplo (`admin/admin` del perfil dev). Ver el punto 8.

## 5. Stack

- JHipster 9.2.0 con Spring Boot 4, Java 21 y Maven Wrapper (`./mvnw`).
- PostgreSQL, con la versión definida por la configuración Docker de JHipster.
- Node 22 LTS, usado solo por las herramientas de JHipster.
- Kafka con `spring-kafka`; Redis con Lettuce.
- App: Kotlin Multiplatform, con librerías a definir antes de empezar la app.
- Secretos (JWT técnico, credenciales de Redis y Kafka) solo en variables de entorno,
  nunca en el repositorio.

## 6. Las horas de la agenda se guardan sin zona horaria

JHipster configura `hibernate.jdbc.time_zone: UTC`, que convierte todas las fechas y
horas a UTC al guardarlas. Para los horarios de atención (`WeeklySchedule.startTime` y
`endTime`) eso está mal: "atiende a las 9" es una hora de reloj, no un instante.
Con la conversión, un horario de 18 a 21 quedaba guardado como 21 a 00 y no aparecía al
buscar por la tarde.

- Decisión: esas dos columnas usan `@JdbcTypeCode(SqlTypes.LOCAL_TIME)` y se guardan tal
  cual. Las fechas de auditoría (`createdAt`, `updatedAt`) siguen en UTC, porque sí son
  instantes.
- Decisión: el filtro de disponibilidad compara las horas en Java y no en la consulta,
  porque los parámetros de tipo hora también se convertían.
- Descartado: sacar `hibernate.jdbc.time_zone` de toda la aplicación, porque los
  `Instant` sí tienen que guardarse en UTC.

## 7. Contraseñas con Argon2id

> Desde el punto 8 el catálogo no tiene usuarios, así que esta decisión ya no aplica acá.
> Sigue vigente en turnos, donde se registran los usuarios finales.

Las contraseñas se guardan con Argon2id (`Argon2PasswordEncoder` de Spring Security).

- Motivo: bcrypt hace lento cada intento, pero usa muy poca memoria, así que una GPU
  puede probar miles en paralelo. Argon2id obliga a usar memoria en cada intento
  (16 MiB con la configuración de Spring), lo que limita ese paralelismo. Es la primera
  recomendación de OWASP para guardar contraseñas.
- En el catálogo solo afecta al usuario de ejemplo del perfil de desarrollo. En turnos,
  donde se registran los usuarios finales, se usa desde el principio.
- Descartado: bcrypt, que sigue siendo aceptable pero quedó como opción para sistemas
  que ya lo usan.

## 8. El catálogo no tiene login

Se sacaron el `POST /api/authenticate` y el usuario en memoria (`admin/admin` en desarrollo,
`test/test` en los tests). El catálogo ya no emite tokens: solo valida los que emite turnos.
Queda el `GET /api/authenticate`, que responde si el token del pedido es válido.

- Motivo: los dos servicios comparten el secreto de los JWT (punto 4). Un token emitido por el
  catálogo para `admin`, con rol `ADMIN`, también era válido en turnos, y le daba acceso a la
  administración de usuarios. Además, la contraseña de ese usuario es pública.
- Con esto también se va Argon2 del catálogo (punto 7) y la dependencia de Bouncy Castle.
- Descartado: dejar el login solo en el perfil de desarrollo. Sigue siendo una forma de crear
  tokens válidos para turnos, y el perfil de desarrollo usa el mismo secreto que se configure.

## 9. Servicios de solo lectura y DTO como `record`

Desde que el catálogo es de solo lectura no hay endpoints para crear, modificar ni borrar.
Pero quedaba el código que JHipster genera para eso: métodos `save`, `update`, `partialUpdate`
y `delete` en los servicios, y los mappers de MapStruct con la conversión de DTO a entidad.
Nadie lo llamaba y no tenía tests.

- Decisión: los servicios de categorías, profesionales y horarios quedan solo con consultas
  (`@Transactional(readOnly = true)`).
- Decisión: los DTO son `record` con un método `from(entidad)`. Un `record` no se puede
  modificar después de creado, que es lo que corresponde a una respuesta de solo lectura.
- Con eso se va MapStruct: la única conversión que queda es de entidad a DTO, y escrita a mano
  son unas pocas líneas que se leen y se prueban directo (`ProfessionalDTOTest`,
  `WeeklyScheduleDTOTest`).
- En un profesional, de la categoría viajan solo el id y el nombre; en un horario, del
  profesional viaja solo el id. Antes viajaba el objeto completo con los demás campos en
  `null`.
- Descartado: dejar el código de escritura "por si hace falta". La copia local solo la
  modifica la sincronización (`JpaCatalogReplica`); un `save` suelto en un servicio sería una
  segunda forma de escribir, sin las reglas de versiones.

## 10. Chequeo de salud de Redis desactivado y Compose propio

- `/management/health` daba siempre `DOWN` (503) aunque el servicio funcionara. El chequeo de
  Redis que trae Spring usa un comando que el usuario de Redis de la cátedra no tiene
  permitido, y ese chequeo forma parte del estado general.
- Decisión: se desactiva (`management.health.redis.enabled: false`). Si Redis no responde, se
  ve donde corresponde: `GET /api/catalog/sync-status` pasa a `ERROR` con el motivo.
- Descartado: escribir un chequeo propio que lea una clave de Redis. Sería repetir lo que ya
  informa el estado de la sincronización.
- El catálogo tiene su propio `docker/compose.yml` (el servicio y su base) para poder probarlo
  solo. El del sistema completo sigue en el repo de turnos.
- Las claves de Redis de la cátedra quedaron como constantes con nombre en
  `RedisCatalogChangeSource`, en lugar de armarse concatenando texto en cada llamada.

## 11. Entidades de la copia: superclase común y fuera de `domain`

Las tres entidades que se copian de la cátedra (`ProfessionalCategory`, `Professional` y
`WeeklySchedule`) repetían los mismos cuatro campos con sus getters y setters: el id de la
cátedra, si están habilitadas y las dos fechas. Además JHipster las deja en un paquete
`domain`, y el nombre confunde: tienen anotaciones de JPA y validación, así que no son un
dominio independiente del framework.

- Decisión: lo común pasa a `CatalogEntity`, una superclase con `@MappedSuperclass`. Cada
  entidad queda solo con lo suyo. El id local sigue declarado en cada una, para no cambiar
  cómo se generan los ids.
- Decisión: se sacan los setters encadenados (`.firstName(...).lastName(...)`) que genera
  JHipster. Solo los usaban los tests y duplicaban cada setter.
- Decisión: las entidades pasan a `repository.entity`, junto a sus repositorios. Son el modelo
  de persistencia de la copia. El único paquete `domain` que queda es `sync.domain` (el
  snapshot, los cambios y las versiones), que no depende de Spring, JPA ni Jackson.
- Las entidades ya no tienen anotaciones de Jackson: la API devuelve los DTO (punto 9), nunca
  una entidad.
- Se borró `AbstractAuditingEntity`, que venía con JHipster y ninguna entidad usaba.
- Descartado: armar un dominio aparte para las búsquedas. Son consultas de solo lectura sobre
  la copia, sin reglas propias; separar dominio y persistencia ahí sería duplicar clases sin
  ganar nada.

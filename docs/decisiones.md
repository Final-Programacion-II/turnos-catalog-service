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

Las contraseñas se guardan con Argon2id (`Argon2PasswordEncoder` de Spring Security).

- Motivo: bcrypt hace lento cada intento, pero usa muy poca memoria, así que una GPU
  puede probar miles en paralelo. Argon2id obliga a usar memoria en cada intento
  (16 MiB con la configuración de Spring), lo que limita ese paralelismo. Es la primera
  recomendación de OWASP para guardar contraseñas.
- En el catálogo solo afecta al usuario de ejemplo del perfil de desarrollo. En turnos,
  donde se registran los usuarios finales, se usa desde el principio.
- Descartado: bcrypt, que sigue siendo aceptable pero quedó como opción para sistemas
  que ya lo usan.

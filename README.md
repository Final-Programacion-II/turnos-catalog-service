<!-- calidad:inicio -->
![Calidad](https://img.shields.io/badge/Calidad-53%2F100-yellow) ![Aprobar](https://img.shields.io/badge/Aprobar-no-red) ![Destacar](https://img.shields.io/badge/Destacar-no-lightgrey) ![Build](https://img.shields.io/badge/Build-OK-green) ![Duplicación](https://img.shields.io/badge/Duplicaci%C3%B3n-7.8%25-orange) ![%POO](https://img.shields.io/badge/%25POO-80%25-green) ![Sueltas](https://img.shields.io/badge/Sueltas-0-green) ![D1](https://img.shields.io/badge/D1-2-red) ![D2](https://img.shields.io/badge/D2-0-red) ![D4](https://img.shields.io/badge/D4-90-green) ![D5](https://img.shields.io/badge/D5-70-yellow) ![D6](https://img.shields.io/badge/D6-100-green) ![D7](https://img.shields.io/badge/D7-77-yellow)

**Calidad (heurístico, 7 dimensiones):** índice **53/100** · build **OK** · aprobar **no (3/11)** · destacar **no**
Detalle completo en [`docs/EVALUACION.md`](docs/EVALUACION.md).
<!-- calidad:fin -->

# turnos-catalog-service

Servicio de catálogo del sistema distribuido de turnos (Proyecto integrador 2026,
Programación II, UM). Mantiene una copia local de las categorías, profesionales y horarios
semanales de la cátedra, la mantiene al día (REST, Redis y Kafka) y responde las búsquedas
del catálogo y la agenda que usa el servicio de turnos.

El sistema tiene tres repositorios:

| Repo                                                                                                | Qué hace                                                                         |
| --------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------- |
| **turnos-catalog-service** (este)                                                                   | Copia local del catálogo de la cátedra y búsquedas.                              |
| [turnos-appointments-service](https://github.com/Final-Programacion-II/turnos-appointments-service) | Usuarios, disponibilidad y reservas. Tiene el Docker Compose de todo el sistema. |
| [turnos-kmp-app](https://github.com/Final-Programacion-II/turnos-kmp-app)                           | App Android con Kotlin Multiplatform.                                            |

La arquitectura completa del sistema está en el repo de turnos
([arquitectura.md](https://github.com/Final-Programacion-II/turnos-appointments-service/blob/main/docs/arquitectura.md)).

## Qué hace y qué no

- Guarda su propia copia del catálogo en su base PostgreSQL. Las búsquedas nunca van al
  servidor de la cátedra.
- Se sincroniza solo: al arrancar, cuando llega un aviso por Kafka y cada 5 minutos como
  respaldo. El detalle está en [docs/sincronizacion.md](docs/sincronizacion.md).
- Es de solo lectura: no tiene endpoints para crear, modificar ni borrar. El catálogo se
  cambia en la cátedra y llega por la sincronización.
- No tiene usuarios ni login. Valida los JWT que emite el servicio de turnos (ver
  [decisiones.md](docs/decisiones.md), punto 8).

## Requisitos

- Java 21 y Docker (para PostgreSQL y para los tests con Testcontainers).
- ZeroTier conectado a la red de la cátedra, con la cuenta de integración ya registrada.

## Configuración

Las URLs y los secretos van en variables de entorno, nunca en el repositorio:

```bash
cp .env.example .env
chmod 600 .env
```

Hay que completar el `.env` con los datos de la cuenta de integración (API, JWT técnico,
Redis y Kafka) y con el secreto de los JWT. Ese secreto tiene que ser el mismo que el del
servicio de turnos. Cada variable está explicada en `.env.example`.

## Levantar en desarrollo

```bash
set -a; source .env; set +a
./mvnw
```

Queda en `http://localhost:8081`. Spring levanta solo el PostgreSQL de desarrollo con Docker
(`src/main/docker/services.yml`). Si la base está vacía, al arrancar descarga el catálogo
completo de la cátedra.

Para ver si está sincronizado (con el JWT de un usuario de turnos):

```bash
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/catalog/sync-status
```

## Levantar el sistema completo con Docker

El Docker Compose de los dos servicios está en el repo de turnos
([docs/docker.md](https://github.com/Final-Programacion-II/turnos-appointments-service/blob/main/docs/docker.md)).
La imagen de este servicio se construye acá:

```bash
./mvnw -ntp -Pprod verify jib:dockerBuild -DskipTests
```

## Tests

Los tests de integración usan Testcontainers, así que necesitan Docker.

```bash
./mvnw verify                                  # todos: unitarios e integración
./mvnw test                                    # solo los unitarios
./mvnw test -Dtest=CatalogSyncServiceTest      # una clase de tests unitarios
./mvnw verify -Dit.test=ProfessionalSearchIT   # una clase de tests de integración
```

El CI de GitHub corre `./mvnw verify` en cada PR (`.github/workflows/ci.yml`).

Qué cubren los tests de cada parte:

| Parte                                 | Tests                                                                                                                                                                                 |
| ------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Decidir entre snapshot y cambios      | `CatalogSyncServiceTest`, `CatalogVersionsTest`, `CatalogSnapshotTest`                                                                                                                |
| Cliente REST y Redis de la cátedra    | `CatedraRestAdapterTest`, `RedisCatalogChangeSourceIT`                                                                                                                                |
| Guardar la copia local                | `JpaCatalogReplicaIT`, `CatalogSyncStateRepositoryIT`, `CatalogSyncIT`                                                                                                                |
| Aviso por Kafka                       | `CatalogUpdatedListenerIT`                                                                                                                                                            |
| Arranque, respaldo periódico y estado | `CatalogSyncOnStartupTest`, `CatalogSyncSchedulerTest`, `RecordingCatalogSyncTest`, `CatalogSyncStatusIT`                                                                             |
| Búsquedas y agenda                    | `ProfessionalSearchIT`, `ProfessionalAgendaIT`, `ProfessionalResourceIT`, `ProfessionalCategoryResourceIT`, `WeeklyScheduleResourceIT`                                                |
| Seguridad                             | `AuthenticateControllerIT`, `TokenAuthenticationIT`, `AccountResourceIT`                                                                                                              |
| Arquitectura                          | `SyncArchitectureTest` (el dominio de la sincronización no depende de Spring, JPA ni Jackson, y el dominio y los puertos no dependen de la infraestructura), `TechnicalStructureTest` |

## Documentación

| Documento                                   | Contenido                                                     |
| ------------------------------------------- | ------------------------------------------------------------- |
| [api.md](docs/api.md)                       | Endpoints, filtros de búsqueda, contrato con turnos y errores |
| [modelo-datos.md](docs/modelo-datos.md)     | Tablas del servicio, de dónde salen los datos y versionado    |
| [sincronizacion.md](docs/sincronizacion.md) | Cómo se mantiene al día la copia local                        |
| [decisiones.md](docs/decisiones.md)         | Decisiones técnicas, con motivos y alternativas descartadas   |
| [adr/](docs/adr/)                           | Registros de decisiones de arquitectura                       |
| [catedra/](docs/catedra/)                   | Enunciado y referencia de integración de la cátedra           |

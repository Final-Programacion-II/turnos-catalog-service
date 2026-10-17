<!-- evaluacion:inicio -->
# Evaluación — Final-Programacion-II/turnos-catalog-service

> Documento **generado automáticamente** por la cátedra de Programación II (UM): RepoSense + análisis de calidad de **7 dimensiones**. **No editar a mano**: se regenera en cada evaluación.
> Rama evaluada: `main`. Análisis **heurístico** (regex + métricas) + build: es una criba para orientar la revisión, **no una nota**. La **documentación** del repositorio no se evalúa.
> Generado: 2026-10-17.

## 1. Veredicto

| Aspecto | Resultado |
|---|---|
| Índice de calidad | **53/100** |
| Tipo de repositorio | `backend` |
| Build (`mvnw verify` / `gradlew check`) | OK |
| Código (LOC) | 6,583 |
| Archivos de código | 107 |
| Clases | 102 |
| Código dentro de clases (%POO) | 83% |
| Funciones fuera de clases | 0 |
| Duplicación | 31.3% |
| Cobertura | 68% |
| Tope duro aplicado a D1 | `DUP>=30%` → D1 ≤ 30 |

> Este informe **describe** lo que el análisis midió con los criterios ya establecidos de la materia. No es una nota.

## 2. Semáforo de aprobación

Criterios de la cátedra (`CRITERIOS-CALIDAD.md`): **Aprobar** se juega con el perfil `estandar`; **destacar**, con el `exigente` (meta de excelencia).

| Semáforo | Resultado |
|---|---|
| Aprobar (`estandar`) | **no cumple** — 4/11 |
| Destacar (`exigente`) | **no cumple** — 4/11 |

**Para aprobar, falta cumplir:** SOLID; Hexagonal; Duplicación; Cobertura

**Para destacar, falta cumplir:** SOLID; Hexagonal; Duplicación; Cobertura

## 3. Autoría (RepoSense)

| Autor | Commits | Código (LOC) | Tests (LOC) | Docs (LOC) | Días activos | % del código |
|---|---:|---:|---:|---:|---:|---:|
| Facundo Mangione | 6 | 4,005 | 4,243 | 1,332 | 4 | 100% |

> _El LOC de autoría lo calcula RepoSense sobre el historial y puede diferir del LOC de código del análisis (que mira el estado de `main`)._

## 4. Dimensiones evaluadas

Cada dimensión indica **qué mide**, **por qué importa** y **tu resultado**. Cuando el puntaje queda por debajo de la referencia, se agregan preguntas para **investigar la causa** (no hay recetas).

| Dimensión | Peso | Puntaje |
|:--|--:|--:|
| **D1** Diseño OO, SOLID y patrones (20%) | 20% | 3 |
| **D2** Arquitectura hexagonal — backend (15%) | 15% | 0 |
| **D3** Frontend KMP: MVVM y buenas prácticas (10%) | 10% | N/A |
| **D4** Integración y contrato (10%) | 10% | 90 |
| **D5** Seguridad y robustez (10%) | 10% | 95 |
| **D6** Pruebas y cobertura (20%) | 20% | 97 |
| **D7** Proceso y entrega (15%) | 15% | 62 |

### D1. Diseño OO, SOLID y patrones (20%)

- **Qué mide:** Métricas orientadas a objetos (clases, métodos, campos, cohesión, acoplamiento, herencia) y proxies de SOLID, más patrones de diseño.
- **Por qué importa:** El eje de la materia es el buen uso de la POO y su arquitectura: un diseño con clases claras y poco acopladas es más fácil de entender, probar y cambiar.
- **Tu resultado:** **3/100**
- **Para investigar la causa:**
    - ¿Se pensó el modelo de clases antes de codificar o se agrupó después?
    - ¿Qué clases concentran muchas responsabilidades a la vez?
    - ¿Los colaboradores llegan como parámetros o se crean por dentro?

### D2. Arquitectura hexagonal — backend (15%)

- **Qué mide:** Separación en capas (dominio, puertos de entrada/salida, adaptadores) y dirección de las dependencias del backend.
- **Por qué importa:** Aislar el dominio de la infraestructura permite cambiar la base de datos, el framework o el transporte sin tocar las reglas de negocio.
- **Tu resultado:** **0/100**
- **Para investigar la causa:**
    - ¿El dominio importa framework o detalles de persistencia?
    - ¿Dónde vive la lógica de negocio frente a los controladores?
    - ¿Se definieron puertos para los servicios que se consumen?

### D4. Integración y contrato (10%)

- **Qué mide:** Cumplimiento del contrato de integración de la cátedra (formato de errores, paginación, autenticación, endpoints esperados).
- **Por qué importa:** Los servicios deben poder integrarse entre sí y con la app sin sorpresas; el contrato es lo que hacen cumplir las pruebas de integración.
- **Tu resultado:** **90/100**

### D5. Seguridad y robustez (10%)

- **Qué mide:** Credenciales embebidas, consultas SQL por concatenación, y presencia de config de entorno; aislamiento de secretos.
- **Por qué importa:** Un secreto en el repositorio o una consulta construida por concatenación son riesgos concretos, no hipotéticos.
- **Tu resultado:** **95/100**

### D6. Pruebas y cobertura (20%)

- **Qué mide:** Cantidad y resultado de pruebas y cobertura de línea (JaCoCo/Kover, Surefire/Failsafe) cuando hay build.
- **Por qué importa:** Las pruebas son la red de seguridad para refactorizar e integrar; la cobertura acota las zonas sin verificar.
- **Tu resultado:** **97/100**

### D7. Proceso y entrega (15%)

- **Qué mide:** Artefactos verificables de proceso/entrega: `compose`, `.env.example`, CI (`.github/workflows/`), `CODEOWNERS` y wrapper de build (`mvnw`/`gradlew`). **No** puntúa documentación.
- **Por qué importa:** Un proyecto que compila, se levanta y se valida solo es más fácil de revisar y de integrar en equipo.
- **Tu resultado:** **62/100**

## 5. Señales detectadas

El análisis encontró las siguientes señales (archivo, sin número de línea a propósito):

**SRP/responsabilidad** (42)
- clase 'CatalogApp' grande (LOC=80, métodos=10, campos=2) — `src/main/java/ar/edu/um/turnos/catalog/CatalogApp.java`
- método 'CatalogApp.logApplicationStartup' de 35 líneas — `src/main/java/ar/edu/um/turnos/catalog/CatalogApp.java`
- clase 'SecurityUtils' grande (LOC=122, métodos=13, campos=2) — `src/main/java/ar/edu/um/turnos/catalog/security/SecurityUtils.java`
- método 'SecurityUtils.extractDetailsFromTokenAttributes' de 39 líneas — `src/main/java/ar/edu/um/turnos/catalog/security/SecurityUtils.java`
- método 'SecurityUtils.extractDetailsFromTokenAttributes' con complejidad 7 — `src/main/java/ar/edu/um/turnos/catalog/security/SecurityUtils.java`
- clase 'LoggingAspect' grande (LOC=89, métodos=11, campos=1) — `src/main/java/ar/edu/um/turnos/catalog/aop/logging/LoggingAspect.java`
- clase 'ProfessionalCategoryDTO' grande (LOC=86, métodos=17, campos=7) — `src/main/java/ar/edu/um/turnos/catalog/service/dto/ProfessionalCategoryDTO.java`
- clase 'ProfessionalCategoryDTO' con baja cohesión (21%) — `src/main/java/ar/edu/um/turnos/catalog/service/dto/ProfessionalCategoryDTO.java`
- clase 'WeeklyScheduleDTO' grande (LOC=114, métodos=23, campos=10) — `src/main/java/ar/edu/um/turnos/catalog/service/dto/WeeklyScheduleDTO.java`
- clase 'WeeklyScheduleDTO' con baja cohesión (15%) — `src/main/java/ar/edu/um/turnos/catalog/service/dto/WeeklyScheduleDTO.java`
- clase 'ProfessionalDTO' grande (LOC=95, métodos=19, campos=8) — `src/main/java/ar/edu/um/turnos/catalog/service/dto/ProfessionalDTO.java`
- clase 'ProfessionalDTO' con baja cohesión (18%) — `src/main/java/ar/edu/um/turnos/catalog/service/dto/ProfessionalDTO.java`
- clase 'SecurityMetersService' grande (LOC=35, métodos=7, campos=8) — `src/main/java/ar/edu/um/turnos/catalog/management/SecurityMetersService.java`
- clase 'SecurityMetersService' con baja cohesión (33%) — `src/main/java/ar/edu/um/turnos/catalog/management/SecurityMetersService.java`
- clase 'WeeklySchedule' grande (LOC=169, métodos=33, campos=11) — `src/main/java/ar/edu/um/turnos/catalog/domain/WeeklySchedule.java`
- clase 'WeeklySchedule' con baja cohesión (11%) — `src/main/java/ar/edu/um/turnos/catalog/domain/WeeklySchedule.java`
- clase 'AbstractAuditingEntity' con baja cohesión (14%) — `src/main/java/ar/edu/um/turnos/catalog/domain/AbstractAuditingEntity.java`
- clase 'ProfessionalCategory' grande (LOC=125, métodos=24, campos=8) — `src/main/java/ar/edu/um/turnos/catalog/domain/ProfessionalCategory.java`
- clase 'ProfessionalCategory' con baja cohesión (15%) — `src/main/java/ar/edu/um/turnos/catalog/domain/ProfessionalCategory.java`
- clase 'Professional' grande (LOC=138, métodos=27, campos=9) — `src/main/java/ar/edu/um/turnos/catalog/domain/Professional.java`
- clase 'Professional' con baja cohesión (13%) — `src/main/java/ar/edu/um/turnos/catalog/domain/Professional.java`
- clase 'AuthenticateController' con baja cohesión (24%) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/AuthenticateController.java`
- clase 'AccountResource' con baja cohesión (11%) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/AccountResource.java`
- clase 'UserVM' con baja cohesión (30%) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/AccountResource.java`
- clase 'ExceptionTranslator' grande (LOC=183, métodos=23, campos=7) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/errors/ExceptionTranslator.java`
- clase 'ExceptionTranslator' con baja cohesión (0%) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/errors/ExceptionTranslator.java`
- método 'ExceptionTranslator.customizeProblem' con complejidad 9 — `src/main/java/ar/edu/um/turnos/catalog/web/rest/errors/ExceptionTranslator.java`
- método 'ExceptionTranslator.getCustomizedErrorDetails' con complejidad 6 — `src/main/java/ar/edu/um/turnos/catalog/web/rest/errors/ExceptionTranslator.java`
- clase 'LoginVM' con baja cohesión (33%) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/vm/LoginVM.java`
- clase 'JwtAuthenticationTestUtils' grande (LOC=57, métodos=10, campos=1) — `src/test/java/ar/edu/um/turnos/catalog/security/jwt/JwtAuthenticationTestUtils.java`
- clase 'TokenAuthenticationSecurityMetersIT' con baja cohesión (48%) — `src/test/java/ar/edu/um/turnos/catalog/security/jwt/TokenAuthenticationSecurityMetersIT.java`
- clase 'DateTimeWrapper' grande (LOC=96, métodos=19, campos=9) — `src/test/java/ar/edu/um/turnos/catalog/repository/timezone/DateTimeWrapper.java`
- clase 'DateTimeWrapper' con baja cohesión (11%) — `src/test/java/ar/edu/um/turnos/catalog/repository/timezone/DateTimeWrapper.java`
- clase 'HibernateTimeZoneIT' grande (LOC=114, métodos=10, campos=8) — `src/test/java/ar/edu/um/turnos/catalog/config/timezone/HibernateTimeZoneIT.java`
- clase 'ProfessionalCategoryResourceIT' grande (LOC=419, métodos=31, campos=23) — `src/test/java/ar/edu/um/turnos/catalog/web/rest/ProfessionalCategoryResourceIT.java`
- clase 'ProfessionalCategoryResourceIT' con baja cohesión (47%) — `src/test/java/ar/edu/um/turnos/catalog/web/rest/ProfessionalCategoryResourceIT.java`
- clase 'ProfessionalResourceIT' grande (LOC=452, métodos=35, campos=25) — `src/test/java/ar/edu/um/turnos/catalog/web/rest/ProfessionalResourceIT.java`
- clase 'WeeklyScheduleResourceIT' grande (LOC=479, métodos=34, campos=28) — `src/test/java/ar/edu/um/turnos/catalog/web/rest/WeeklyScheduleResourceIT.java`
- clase 'TestUtil' grande (LOC=145, métodos=15, campos=2) — `src/test/java/ar/edu/um/turnos/catalog/web/rest/TestUtil.java`
- método 'TestUtil.asDecimal' con complejidad 7 — `src/test/java/ar/edu/um/turnos/catalog/web/rest/TestUtil.java`
- método 'NumberMatcher.asDecimal' con complejidad 7 — `src/test/java/ar/edu/um/turnos/catalog/web/rest/TestUtil.java`
- clase 'ExceptionTranslatorTestController' grande (LOC=47, métodos=10, campos=1) — `src/test/java/ar/edu/um/turnos/catalog/web/rest/errors/ExceptionTranslatorTestController.java`

**DIP/concretos** (47)
- clase 'CatalogApp' muy acoplada (CBO=24) — `src/main/java/ar/edu/um/turnos/catalog/CatalogApp.java`
- clase 'SecurityUtils' muy acoplada (CBO=28) — `src/main/java/ar/edu/um/turnos/catalog/security/SecurityUtils.java`
- clase 'LoggingAspect' muy acoplada (CBO=24) — `src/main/java/ar/edu/um/turnos/catalog/aop/logging/LoggingAspect.java`
- clase 'ProfessionalCategoryService' muy acoplada (CBO=14) — `src/main/java/ar/edu/um/turnos/catalog/service/ProfessionalCategoryService.java`
- clase 'ProfessionalService' muy acoplada (CBO=16) — `src/main/java/ar/edu/um/turnos/catalog/service/ProfessionalService.java`
- clase 'WeeklyScheduleService' muy acoplada (CBO=14) — `src/main/java/ar/edu/um/turnos/catalog/service/WeeklyScheduleService.java`
- clase 'WebConfigurer' muy acoplada (CBO=13) — `src/main/java/ar/edu/um/turnos/catalog/config/WebConfigurer.java`
- clase 'LiquibaseConfiguration' muy acoplada (CBO=18) — `src/main/java/ar/edu/um/turnos/catalog/config/LiquibaseConfiguration.java`
- clase 'CRLFLogConverter' muy acoplada (CBO=16) — `src/main/java/ar/edu/um/turnos/catalog/config/CRLFLogConverter.java`
- clase 'SecurityJwtConfiguration' muy acoplada (CBO=19) — `src/main/java/ar/edu/um/turnos/catalog/config/SecurityJwtConfiguration.java`
- clase 'SecurityConfiguration' muy acoplada (CBO=12) — `src/main/java/ar/edu/um/turnos/catalog/config/SecurityConfiguration.java`
- clase 'AsyncConfiguration' muy acoplada (CBO=12) — `src/main/java/ar/edu/um/turnos/catalog/config/AsyncConfiguration.java`
- clase 'AsyncConfiguration' instancia 3 concretos (CBO=12) — `src/main/java/ar/edu/um/turnos/catalog/config/AsyncConfiguration.java`
- clase 'WeeklySchedule' muy acoplada (CBO=10) — `src/main/java/ar/edu/um/turnos/catalog/domain/WeeklySchedule.java`
- clase 'WeeklyScheduleResource' muy acoplada (CBO=37) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/WeeklyScheduleResource.java`
- clase 'WeeklyScheduleResource' instancia 8 concretos (CBO=37) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/WeeklyScheduleResource.java`
- clase 'AuthenticateController' muy acoplada (CBO=31) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/AuthenticateController.java`
- clase 'AuthenticateController' instancia 3 concretos (CBO=31) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/AuthenticateController.java`
- clase 'AccountResource' muy acoplada (CBO=16) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/AccountResource.java`
- clase 'AccountResource' instancia 3 concretos (CBO=16) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/AccountResource.java`
- clase 'ProfessionalCategoryResource' muy acoplada (CBO=37) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/ProfessionalCategoryResource.java`
- clase 'ProfessionalCategoryResource' instancia 8 concretos (CBO=37) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/ProfessionalCategoryResource.java`
- clase 'ProfessionalResource' muy acoplada (CBO=36) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/ProfessionalResource.java`
- clase 'ProfessionalResource' instancia 8 concretos (CBO=36) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/ProfessionalResource.java`
- clase 'BadRequestAlertException' muy acoplada (CBO=8) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/errors/BadRequestAlertException.java`
- clase 'ExceptionTranslator' muy acoplada (CBO=60) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/errors/ExceptionTranslator.java`
- clase 'TechnicalStructureTest' muy acoplada (CBO=10) — `src/test/java/ar/edu/um/turnos/catalog/TechnicalStructureTest.java`
- clase 'SecurityUtilsUnitTest' muy acoplada (CBO=12) — `src/test/java/ar/edu/um/turnos/catalog/security/SecurityUtilsUnitTest.java`
- clase 'SecurityUtilsUnitTest' instancia 12 concretos (CBO=12) — `src/test/java/ar/edu/um/turnos/catalog/security/SecurityUtilsUnitTest.java`
- clase 'JwtAuthenticationTestUtils' muy acoplada (CBO=20) — `src/test/java/ar/edu/um/turnos/catalog/security/jwt/JwtAuthenticationTestUtils.java`
- clase 'JwtAuthenticationTestUtils' instancia 4 concretos (CBO=20) — `src/test/java/ar/edu/um/turnos/catalog/security/jwt/JwtAuthenticationTestUtils.java`
- clase 'DateTimeWrapper' muy acoplada (CBO=11) — `src/test/java/ar/edu/um/turnos/catalog/repository/timezone/DateTimeWrapper.java`
- clase 'CRLFLogConverterTest' muy acoplada (CBO=12) — `src/test/java/ar/edu/um/turnos/catalog/config/CRLFLogConverterTest.java`
- clase 'CRLFLogConverterTest' instancia 9 concretos (CBO=12) — `src/test/java/ar/edu/um/turnos/catalog/config/CRLFLogConverterTest.java`
- clase 'WebConfigurerTest' muy acoplada (CBO=25) — `src/test/java/ar/edu/um/turnos/catalog/config/WebConfigurerTest.java`
- clase 'WebConfigurerTest' instancia 8 concretos (CBO=25) — `src/test/java/ar/edu/um/turnos/catalog/config/WebConfigurerTest.java`
- clase 'HibernateTimeZoneIT' muy acoplada (CBO=24) — `src/test/java/ar/edu/um/turnos/catalog/config/timezone/HibernateTimeZoneIT.java`
- clase 'ProfessionalCategoryResourceIT' muy acoplada (CBO=28) — `src/test/java/ar/edu/um/turnos/catalog/web/rest/ProfessionalCategoryResourceIT.java`
- clase 'ProfessionalCategoryResourceIT' instancia 6 concretos (CBO=28) — `src/test/java/ar/edu/um/turnos/catalog/web/rest/ProfessionalCategoryResourceIT.java`
- clase 'ProfessionalResourceIT' muy acoplada (CBO=35) — `src/test/java/ar/edu/um/turnos/catalog/web/rest/ProfessionalResourceIT.java`
- clase 'ProfessionalResourceIT' instancia 8 concretos (CBO=35) — `src/test/java/ar/edu/um/turnos/catalog/web/rest/ProfessionalResourceIT.java`
- clase 'WeeklyScheduleResourceIT' muy acoplada (CBO=38) — `src/test/java/ar/edu/um/turnos/catalog/web/rest/WeeklyScheduleResourceIT.java`
- clase 'WeeklyScheduleResourceIT' instancia 6 concretos (CBO=38) — `src/test/java/ar/edu/um/turnos/catalog/web/rest/WeeklyScheduleResourceIT.java`
- clase 'AccountResourceIT' muy acoplada (CBO=8) — `src/test/java/ar/edu/um/turnos/catalog/web/rest/AccountResourceIT.java`
- clase 'TestUtil' muy acoplada (CBO=25) — `src/test/java/ar/edu/um/turnos/catalog/web/rest/TestUtil.java`
- clase 'TestUtil' instancia 6 concretos (CBO=25) — `src/test/java/ar/edu/um/turnos/catalog/web/rest/TestUtil.java`
- clase 'ExceptionTranslatorIT' muy acoplada (CBO=11) — `src/test/java/ar/edu/um/turnos/catalog/web/rest/errors/ExceptionTranslatorIT.java`

**OCP/type-switch** (9)
- clase 'SecurityUtils' con 5 type-checks — `src/main/java/ar/edu/um/turnos/catalog/security/SecurityUtils.java`
- clase 'ProfessionalCategoryDTO' con 1 type-checks — `src/main/java/ar/edu/um/turnos/catalog/service/dto/ProfessionalCategoryDTO.java`
- clase 'WeeklyScheduleDTO' con 1 type-checks — `src/main/java/ar/edu/um/turnos/catalog/service/dto/WeeklyScheduleDTO.java`
- clase 'ProfessionalDTO' con 1 type-checks — `src/main/java/ar/edu/um/turnos/catalog/service/dto/ProfessionalDTO.java`
- clase 'WeeklySchedule' con 1 type-checks — `src/main/java/ar/edu/um/turnos/catalog/domain/WeeklySchedule.java`
- clase 'ProfessionalCategory' con 1 type-checks — `src/main/java/ar/edu/um/turnos/catalog/domain/ProfessionalCategory.java`
- clase 'Professional' con 1 type-checks — `src/main/java/ar/edu/um/turnos/catalog/domain/Professional.java`
- clase 'AccountResource' con 2 type-checks — `src/main/java/ar/edu/um/turnos/catalog/web/rest/AccountResource.java`
- clase 'ExceptionTranslator' con 19 type-checks — `src/main/java/ar/edu/um/turnos/catalog/web/rest/errors/ExceptionTranslator.java`

**POO** (5)
- clase 'SecurityMetersService' con 4 campos públicos (encapsulación) — `src/main/java/ar/edu/um/turnos/catalog/management/SecurityMetersService.java`
- clase 'ErrorConstants' con 5 campos públicos (encapsulación) — `src/main/java/ar/edu/um/turnos/catalog/web/rest/errors/ErrorConstants.java`
- clase 'WeeklyScheduleTestSamples' solo con métodos estáticos (¿utilitaria?) — `src/test/java/ar/edu/um/turnos/catalog/domain/WeeklyScheduleTestSamples.java`
- clase 'ProfessionalTestSamples' solo con métodos estáticos (¿utilitaria?) — `src/test/java/ar/edu/um/turnos/catalog/domain/ProfessionalTestSamples.java`
- clase 'ProfessionalCategoryTestSamples' solo con métodos estáticos (¿utilitaria?) — `src/test/java/ar/edu/um/turnos/catalog/domain/ProfessionalCategoryTestSamples.java`

**duplicación** (1)
- duplicación de bloques: 31.3% del código

**hexagonal** (18)
- domain importa framework/infra: import com.fasterxml.jackson.annotation.JsonIgnoreProperties; — `src/main/java/ar/edu/um/turnos/catalog/domain/WeeklySchedule.java`
- domain importa framework/infra: import jakarta.persistence.*; — `src/main/java/ar/edu/um/turnos/catalog/domain/WeeklySchedule.java`
- domain importa framework/infra: import jakarta.validation.constraints.*; — `src/main/java/ar/edu/um/turnos/catalog/domain/WeeklySchedule.java`
- domain importa framework/infra: import com.fasterxml.jackson.annotation.JsonIgnoreProperties; — `src/main/java/ar/edu/um/turnos/catalog/domain/AbstractAuditingEntity.java`
- domain importa framework/infra: import jakarta.persistence.Column; — `src/main/java/ar/edu/um/turnos/catalog/domain/AbstractAuditingEntity.java`
- domain importa framework/infra: import jakarta.persistence.EntityListeners; — `src/main/java/ar/edu/um/turnos/catalog/domain/AbstractAuditingEntity.java`
- domain importa framework/infra: import jakarta.persistence.MappedSuperclass; — `src/main/java/ar/edu/um/turnos/catalog/domain/AbstractAuditingEntity.java`
- domain importa framework/infra: import org.springframework.data.annotation.CreatedBy; — `src/main/java/ar/edu/um/turnos/catalog/domain/AbstractAuditingEntity.java`
- domain importa framework/infra: import org.springframework.data.annotation.CreatedDate; — `src/main/java/ar/edu/um/turnos/catalog/domain/AbstractAuditingEntity.java`
- domain importa framework/infra: import org.springframework.data.annotation.LastModifiedBy; — `src/main/java/ar/edu/um/turnos/catalog/domain/AbstractAuditingEntity.java`
- domain importa framework/infra: import org.springframework.data.annotation.LastModifiedDate; — `src/main/java/ar/edu/um/turnos/catalog/domain/AbstractAuditingEntity.java`
- domain importa framework/infra: import org.springframework.data.jpa.domain.support.AuditingEntityListener; — `src/main/java/ar/edu/um/turnos/catalog/domain/AbstractAuditingEntity.java`
- domain importa framework/infra: import jakarta.persistence.*; — `src/main/java/ar/edu/um/turnos/catalog/domain/ProfessionalCategory.java`
- domain importa framework/infra: import jakarta.validation.constraints.*; — `src/main/java/ar/edu/um/turnos/catalog/domain/ProfessionalCategory.java`
- domain importa framework/infra: import jakarta.persistence.*; — `src/main/java/ar/edu/um/turnos/catalog/domain/Professional.java`
- domain importa framework/infra: import jakarta.validation.constraints.*; — `src/main/java/ar/edu/um/turnos/catalog/domain/Professional.java`
- falta ports/in (hexagonal)
- falta ports/out (hexagonal)

## 6. Evolución por commit

Métricas calculadas sobre el estado de `main` en **cada commit** (4 commits con trabajo), del primero al último. La calidad usa el perfil `exigente`; el aporte de código lo mide RepoSense. El índice puede **bajar** al crecer el proyecto: se acumulan señales.

```mermaid
xychart-beta
    title "Índice de calidad a lo largo de los commits"
    x-axis ["0602193", "f9dc0a9", "6e9665c", "e93585a"]
    y-axis "Índice" 0 --> 100
    line [0, 15, 15, 43]
```

| # | Fecha | Commit | Autor | LOC | Índice | %POO | Duplic. | Sueltas |
|---|---|---|---|---:|---:|---:|---:|---:|
| 1 | 2026-09-16 | `0602193` | Ing. Daniel Quinteros | 0 | 0 | 0% | 0.0% | 0 |
| 2 | 2026-09-16 | `f9dc0a9` | Ing. Daniel Quinteros | 0 | 15 | 0% | 0.0% | 0 |
| 3 | 2026-10-12 | `6e9665c` | Facundo Mangione | 0 | 15 | 0% | 0.0% | 0 |
| 4 | 2026-10-16 | `e93585a` | Facundo Mangione | 6,583 | 43 | 83% | 31.3% | 0 |

---
_Informe generado por la cátedra. Las dimensiones y umbrales corresponden a `calidad.py` (perfil `exigente`); el semáforo, a `CRITERIOS-CALIDAD.md`. La documentación del repositorio no se evalúa. Ante dudas, revisá el archivo señalado._
<!-- evaluacion:fin -->

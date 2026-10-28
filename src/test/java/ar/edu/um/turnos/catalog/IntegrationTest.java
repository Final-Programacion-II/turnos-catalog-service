package ar.edu.um.turnos.catalog;

import ar.edu.um.turnos.catalog.config.AsyncSyncConfiguration;
import ar.edu.um.turnos.catalog.config.DatabaseTestcontainer;
import ar.edu.um.turnos.catalog.config.JacksonConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = {
        CatalogApp.class,
        JacksonConfiguration.class,
        AsyncSyncConfiguration.class,
        ar.edu.um.turnos.catalog.config.JacksonHibernateConfiguration.class,
    },
    // Si en la terminal quedaron cargadas las variables del .env, los tests se conectarían a la cátedra real.
    // Estas propiedades tienen prioridad sobre las variables de entorno; los tests que necesitan una cátedra
    // la levantan ellos mismos con @DynamicPropertySource.
    properties = { "catedra.base-url=", "catedra.token=", "catedra.kafka.enabled=false" }
)
@ImportTestcontainers(DatabaseTestcontainer.class)
public @interface IntegrationTest {}

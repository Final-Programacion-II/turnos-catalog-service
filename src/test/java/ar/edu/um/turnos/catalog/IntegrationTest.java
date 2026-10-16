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
    }
)
@ImportTestcontainers(DatabaseTestcontainer.class)
public @interface IntegrationTest {}

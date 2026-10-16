package ar.edu.um.turnos.catalog.sync.infrastructure.startup;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class CatalogSyncOnStartupTest {

    @Test
    void siFallaLaAppSigueArrancando() {
        CatalogSyncOnStartup listener = new CatalogSyncOnStartup(() -> {
            throw new IllegalStateException("Falta configurar CATEDRA_BASE_URL o CATEDRA_TOKEN");
        });

        assertThatCode(listener::onApplicationReady).doesNotThrowAnyException();
    }
}

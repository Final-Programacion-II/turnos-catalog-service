package ar.edu.um.turnos.catalog.sync.infrastructure.scheduling;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class CatalogSyncSchedulerTest {

    @Test
    void unErrorNoCortaLaTareaPeriodica() {
        // Si la excepción saliera del método, Spring igual reintenta, pero ensucia el log con el stack trace completo
        CatalogSyncScheduler scheduler = new CatalogSyncScheduler(() -> {
            throw new IllegalStateException("La cátedra no responde");
        });

        assertThatCode(scheduler::synchronizePeriodically).doesNotThrowAnyException();
    }
}

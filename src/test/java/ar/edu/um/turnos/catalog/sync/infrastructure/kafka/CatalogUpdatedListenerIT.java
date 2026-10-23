package ar.edu.um.turnos.catalog.sync.infrastructure.kafka;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.um.turnos.catalog.IntegrationTest;
import ar.edu.um.turnos.catalog.sync.ports.in.SynchronizeCatalogUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@IntegrationTest
class CatalogUpdatedListenerIT {

    @Autowired
    private CatalogUpdatedListener listener;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private SynchronizeCatalogUseCase synchronizeCatalog;

    // La app sincroniza al arrancar, así que se limpian también esas llamadas al mock.
    @BeforeEach
    void limpiar() {
        jdbc.update("delete from processed_catalog_event");
        clearInvocations(synchronizeCatalog);
    }

    @Test
    void procesaUnAviso() {
        listener.onCatalogUpdated(evento("evento-1", "CatalogUpdated"));

        verify(synchronizeCatalog, times(1)).synchronize();
    }

    @Test
    void ignoraUnAvisoRepetido() {
        listener.onCatalogUpdated(evento("evento-2", "CatalogUpdated"));
        listener.onCatalogUpdated(evento("evento-2", "CatalogUpdated"));

        verify(synchronizeCatalog, times(1)).synchronize();
    }

    @Test
    void siFallaElEventoSePuedeReintentar() {
        when(synchronizeCatalog.synchronize()).thenThrow(new IllegalStateException("Redis no responde")).thenReturn(8L);

        listener.onCatalogUpdated(evento("evento-3", "CatalogUpdated"));
        listener.onCatalogUpdated(evento("evento-3", "CatalogUpdated"));

        verify(synchronizeCatalog, times(2)).synchronize();
    }

    @Test
    void ignoraOtrosTiposDeEvento() {
        listener.onCatalogUpdated(evento("evento-4", "OtroEvento"));

        verify(synchronizeCatalog, never()).synchronize();
    }

    private String evento(String id, String tipo) {
        return (
            "{\"eventId\":\"" +
            id +
            "\",\"eventType\":\"" +
            tipo +
            "\",\"newVersion\":8,\"occurredAt\":\"2026-09-10T13:44:59Z\",\"source\":\"MANUAL\",\"schemaVersion\":1}"
        );
    }
}

package ar.edu.um.turnos.catalog.web.rest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.um.turnos.catalog.IntegrationTest;
import ar.edu.um.turnos.catalog.domain.CatalogSyncState;
import ar.edu.um.turnos.catalog.repository.CatalogSyncStateRepository;
import ar.edu.um.turnos.catalog.sync.ports.in.SynchronizeCatalogUseCase;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

// Sin @Transactional: el estado se guarda en su propia transacción y eso es parte de lo que se prueba.
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CatalogSyncStatusIT {

    private static final String URL = "/api/catalog/sync-status";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CatalogSyncStateRepository repository;

    @Autowired
    private SynchronizeCatalogUseCase synchronizeCatalog;

    @BeforeEach
    @AfterEach
    void limpiar() {
        guardarEstado(null, null, null);
    }

    @Test
    void sinVersionInformaQueNuncaSeSincronizo() throws Exception {
        mockMvc
            .perform(get(URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("NOT_SYNCED"))
            .andExpect(jsonPath("$.catalogVersion").value(nullValue()));
    }

    @Test
    void conVersionYSinErrorEstaOk() throws Exception {
        guardarEstado(3L, Instant.now(), null);

        mockMvc
            .perform(get(URL))
            .andExpect(jsonPath("$.status").value("OK"))
            .andExpect(jsonPath("$.catalogVersion").value(3))
            .andExpect(jsonPath("$.lastSyncedAt").value(notNullValue()))
            .andExpect(jsonPath("$.lastError").value(nullValue()));
    }

    @Test
    void unaSincronizacionFallidaQuedaRegistrada() throws Exception {
        // En los tests no hay cátedra configurada, así que pedir el snapshot falla
        assertThatThrownBy(() -> synchronizeCatalog.synchronize()).isInstanceOf(RuntimeException.class);

        mockMvc
            .perform(get(URL))
            .andExpect(jsonPath("$.status").value("ERROR"))
            .andExpect(jsonPath("$.lastAttemptAt").value(notNullValue()))
            .andExpect(jsonPath("$.lastError").value(containsString("CATEDRA_BASE_URL")));
    }

    private void guardarEstado(Long version, Instant ultimaSincronizacion, String error) {
        CatalogSyncState state = repository.findById(CatalogSyncState.SINGLETON_ID).orElseThrow();
        state.setCatalogVersion(version);
        state.setLastSyncedAt(ultimaSincronizacion);
        state.setLastAttemptAt(null);
        state.setLastError(error);
        repository.save(state);
    }
}

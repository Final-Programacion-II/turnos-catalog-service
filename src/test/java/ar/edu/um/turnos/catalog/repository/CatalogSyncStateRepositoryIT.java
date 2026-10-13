package ar.edu.um.turnos.catalog.repository;

import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.um.turnos.catalog.IntegrationTest;
import ar.edu.um.turnos.catalog.domain.CatalogSyncState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@Transactional
class CatalogSyncStateRepositoryIT {

    @Autowired
    private CatalogSyncStateRepository repository;

    @Test
    void laMigracionCreaLaFilaInicialSinVersion() {
        CatalogSyncState state = repository.findById(CatalogSyncState.SINGLETON_ID).orElseThrow();

        assertThat(state.getCatalogVersion()).isNull();
        assertThat(state.getLastSyncedAt()).isNull();
    }
}

package ar.edu.um.turnos.catalog.web.rest;

import ar.edu.um.turnos.catalog.domain.CatalogSyncState;
import ar.edu.um.turnos.catalog.repository.CatalogSyncStateRepository;
import ar.edu.um.turnos.catalog.service.dto.CatalogSyncStatusDTO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Informa el estado y el último error de la sincronización con la cátedra.
 */
@RestController
@RequestMapping("/api/catalog")
public class CatalogSyncStatusResource {

    private final CatalogSyncStateRepository repository;

    public CatalogSyncStatusResource(CatalogSyncStateRepository repository) {
        this.repository = repository;
    }

    /**
     * {@code GET /catalog/sync-status} : versión local, última sincronización correcta y último error.
     */
    @GetMapping("/sync-status")
    @Transactional(readOnly = true)
    public CatalogSyncStatusDTO getSyncStatus() {
        CatalogSyncState state = repository.findById(CatalogSyncState.SINGLETON_ID).orElseThrow();
        return new CatalogSyncStatusDTO(
            statusOf(state),
            state.getCatalogVersion(),
            state.getLastSyncedAt(),
            state.getLastAttemptAt(),
            state.getLastError()
        );
    }

    private static String statusOf(CatalogSyncState state) {
        if (state.getLastError() != null) {
            return "ERROR";
        }
        return state.getCatalogVersion() == null ? "NOT_SYNCED" : "OK";
    }
}

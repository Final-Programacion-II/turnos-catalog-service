package ar.edu.um.turnos.catalog.sync.infrastructure.status;

import ar.edu.um.turnos.catalog.domain.CatalogSyncState;
import ar.edu.um.turnos.catalog.repository.CatalogSyncStateRepository;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Guarda cómo terminó cada intento de sincronización, para poder informarlo después.
 * Corre en su propia transacción: si la sincronización falló y se revirtió, el error igual queda guardado.
 */
@Component
@Transactional
public class SyncStatusRecorder {

    private static final int MAX_ERROR_LENGTH = 1000;

    private final CatalogSyncStateRepository repository;

    public SyncStatusRecorder(CatalogSyncStateRepository repository) {
        this.repository = repository;
    }

    public void recordSuccess() {
        CatalogSyncState state = state();
        Instant now = Instant.now();
        state.setLastAttemptAt(now);
        state.setLastSyncedAt(now);
        state.setLastError(null);
    }

    public void recordFailure(RuntimeException error) {
        CatalogSyncState state = state();
        state.setLastAttemptAt(Instant.now());
        state.setLastError(describe(error));
    }

    private CatalogSyncState state() {
        return repository.findById(CatalogSyncState.SINGLETON_ID).orElseThrow();
    }

    private static String describe(RuntimeException error) {
        String message = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
        return message.length() > MAX_ERROR_LENGTH ? message.substring(0, MAX_ERROR_LENGTH) : message;
    }
}

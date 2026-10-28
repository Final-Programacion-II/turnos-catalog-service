package ar.edu.um.turnos.catalog.sync.infrastructure.status;

import ar.edu.um.turnos.catalog.sync.ports.in.SynchronizeCatalogUseCase;

/**
 * Envuelve la sincronización para guardar el resultado de cada intento.
 * Es synchronized porque la pueden disparar a la vez el arranque, Kafka y la tarea periódica,
 * y dos sincronizaciones en paralelo podrían intentar aplicar la misma versión.
 */
public class RecordingCatalogSync implements SynchronizeCatalogUseCase {

    private final SynchronizeCatalogUseCase delegate;
    private final SyncStatusRecorder status;

    public RecordingCatalogSync(SynchronizeCatalogUseCase delegate, SyncStatusRecorder status) {
        this.delegate = delegate;
        this.status = status;
    }

    @Override
    public synchronized long synchronize() {
        try {
            long version = delegate.synchronize();
            status.recordSuccess();
            return version;
        } catch (RuntimeException e) {
            status.recordFailure(e);
            throw e;
        }
    }
}

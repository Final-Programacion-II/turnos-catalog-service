package ar.edu.um.turnos.catalog.sync.application;

import ar.edu.um.turnos.catalog.sync.domain.CatalogSnapshot;
import ar.edu.um.turnos.catalog.sync.ports.in.SynchronizeCatalogUseCase;
import ar.edu.um.turnos.catalog.sync.ports.out.CatalogReplica;
import ar.edu.um.turnos.catalog.sync.ports.out.CatalogSnapshotSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatalogSyncService implements SynchronizeCatalogUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogSyncService.class);

    private final CatalogSnapshotSource source;
    private final CatalogReplica replica;

    public CatalogSyncService(CatalogSnapshotSource source, CatalogReplica replica) {
        this.source = source;
        this.replica = replica;
    }

    @Override
    public long synchronize() {
        Long version = replica.currentVersion().orElse(null);
        if (version != null) {
            LOG.info("La réplica ya está en la versión {}", version);
            return version;
        }

        LOG.info("La réplica no tiene versión, se carga el snapshot completo");
        CatalogSnapshot snapshot = source.fetchSnapshot();
        snapshot.validateReferences();
        replica.apply(snapshot);
        return snapshot.version();
    }
}

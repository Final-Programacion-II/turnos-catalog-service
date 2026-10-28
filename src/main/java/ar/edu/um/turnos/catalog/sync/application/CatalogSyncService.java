package ar.edu.um.turnos.catalog.sync.application;

import ar.edu.um.turnos.catalog.sync.domain.CatalogSnapshot;
import ar.edu.um.turnos.catalog.sync.domain.CatalogVersions;
import ar.edu.um.turnos.catalog.sync.ports.in.SynchronizeCatalogUseCase;
import ar.edu.um.turnos.catalog.sync.ports.out.CatalogChangeSource;
import ar.edu.um.turnos.catalog.sync.ports.out.CatalogReplica;
import ar.edu.um.turnos.catalog.sync.ports.out.CatalogSnapshotSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatalogSyncService implements SynchronizeCatalogUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogSyncService.class);

    private final CatalogSnapshotSource snapshots;
    private final CatalogChangeSource changes;
    private final CatalogReplica replica;

    public CatalogSyncService(CatalogSnapshotSource snapshots, CatalogChangeSource changes, CatalogReplica replica) {
        this.snapshots = snapshots;
        this.changes = changes;
        this.replica = replica;
    }

    @Override
    public long synchronize() {
        Long local = replica.currentVersion().orElse(null);
        if (local == null) {
            LOG.info("La réplica no tiene versión, se carga el snapshot completo");
            return loadSnapshot();
        }

        CatalogVersions versions = changes.versions();
        if (versions.isUpToDate(local)) {
            LOG.info("La réplica ya está en la versión {}", local);
            return local;
        }
        if (!versions.canContinueFrom(local)) {
            LOG.warn(
                "Falta una versión entre la local {} y la más vieja disponible {}, se carga el snapshot",
                local,
                versions.oldestAvailable()
            );
            return loadSnapshot();
        }

        // Una versión por vez: si falla una, las anteriores ya quedaron guardadas.
        for (long version = local + 1; version <= versions.current(); version++) {
            replica.applyChange(changes.fetchChange(version));
        }
        LOG.info("Réplica actualizada de la versión {} a la {}", local, versions.current());
        return versions.current();
    }

    private long loadSnapshot() {
        CatalogSnapshot snapshot = snapshots.fetchSnapshot();
        snapshot.validateReferences();
        replica.apply(snapshot);
        return snapshot.version();
    }
}

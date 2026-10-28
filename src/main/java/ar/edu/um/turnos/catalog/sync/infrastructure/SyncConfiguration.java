package ar.edu.um.turnos.catalog.sync.infrastructure;

import ar.edu.um.turnos.catalog.sync.application.CatalogSyncService;
import ar.edu.um.turnos.catalog.sync.ports.in.SynchronizeCatalogUseCase;
import ar.edu.um.turnos.catalog.sync.ports.out.CatalogChangeSource;
import ar.edu.um.turnos.catalog.sync.ports.out.CatalogReplica;
import ar.edu.um.turnos.catalog.sync.ports.out.CatalogSnapshotSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SyncConfiguration {

    @Bean
    public SynchronizeCatalogUseCase synchronizeCatalogUseCase(
        CatalogSnapshotSource snapshots,
        CatalogChangeSource changes,
        CatalogReplica replica
    ) {
        return new CatalogSyncService(snapshots, changes, replica);
    }
}

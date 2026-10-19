package ar.edu.um.turnos.catalog.sync.infrastructure.startup;

import ar.edu.um.turnos.catalog.sync.ports.in.SynchronizeCatalogUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CatalogSyncOnStartup {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogSyncOnStartup.class);

    private final SynchronizeCatalogUseCase synchronizeCatalog;

    public CatalogSyncOnStartup(SynchronizeCatalogUseCase synchronizeCatalog) {
        this.synchronizeCatalog = synchronizeCatalog;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            long version = synchronizeCatalog.synchronize();
            LOG.info("Catálogo sincronizado en la versión {}", version);
        } catch (RuntimeException e) {
            LOG.warn("No se pudo sincronizar el catálogo al arrancar: {}", e.getMessage());
        }
    }
}

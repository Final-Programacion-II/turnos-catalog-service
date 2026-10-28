package ar.edu.um.turnos.catalog.sync.infrastructure.scheduling;

import ar.edu.um.turnos.catalog.sync.ports.in.SynchronizeCatalogUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Respaldo de Kafka: cada cierto tiempo compara la versión local con Redis.
 * Si se perdió un aviso, o falló la sincronización que disparó, el atraso se recupera acá.
 */
@Component
public class CatalogSyncScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogSyncScheduler.class);

    private final SynchronizeCatalogUseCase synchronizeCatalog;

    public CatalogSyncScheduler(SynchronizeCatalogUseCase synchronizeCatalog) {
        this.synchronizeCatalog = synchronizeCatalog;
    }

    @Scheduled(fixedDelayString = "${catedra.sync.interval:PT5M}", initialDelayString = "${catedra.sync.interval:PT5M}")
    public void synchronizePeriodically() {
        try {
            long version = synchronizeCatalog.synchronize();
            LOG.debug("Sincronización periódica terminada en la versión {}", version);
        } catch (RuntimeException e) {
            LOG.warn("Falló la sincronización periódica: {}", e.getMessage());
        }
    }
}

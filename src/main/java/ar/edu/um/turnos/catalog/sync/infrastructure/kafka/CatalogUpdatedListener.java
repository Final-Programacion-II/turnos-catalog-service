package ar.edu.um.turnos.catalog.sync.infrastructure.kafka;

import ar.edu.um.turnos.catalog.sync.ports.in.SynchronizeCatalogUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Component
public class CatalogUpdatedListener {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogUpdatedListener.class);

    private final SynchronizeCatalogUseCase synchronizeCatalog;
    private final ProcessedEventRegistry processedEvents;
    private final JsonMapper json = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();

    public CatalogUpdatedListener(SynchronizeCatalogUseCase synchronizeCatalog, ProcessedEventRegistry processedEvents) {
        this.synchronizeCatalog = synchronizeCatalog;
        this.processedEvents = processedEvents;
    }

    @KafkaListener(
        topics = "${catedra.kafka.catalog-topic:catedra.catalog.local}",
        groupId = "${catedra.kafka.group-id:catalog-local}",
        autoStartup = "${catedra.kafka.enabled:true}"
    )
    public void onCatalogUpdated(String message) {
        CatalogUpdatedEvent event = json.readValue(message, CatalogUpdatedEvent.class);
        if (!"CatalogUpdated".equals(event.eventType())) {
            LOG.warn("Se ignora un evento de tipo {}", event.eventType());
            return;
        }
        if (processedEvents.wasProcessed(event.eventId())) {
            LOG.info("El evento {} ya fue procesado, se ignora", event.eventId());
            return;
        }

        // El evento se registra solo si la sincronización terminó bien; si falla, el próximo aviso reintenta.
        try {
            long version = synchronizeCatalog.synchronize();
            processedEvents.register(event.eventId());
            LOG.info("Evento {} procesado, réplica en la versión {}", event.eventId(), version);
        } catch (RuntimeException e) {
            LOG.warn("No se pudo sincronizar por el evento {}: {}", event.eventId(), e.getMessage());
        }
    }

    record CatalogUpdatedEvent(String eventId, String eventType, Long newVersion) {}
}

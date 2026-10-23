package ar.edu.um.turnos.catalog.sync.infrastructure.kafka;

import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Recuerda los eventId ya procesados para ignorar los avisos repetidos de Kafka.
// Transaccional porque JHipster configura el pool con auto-commit desactivado: sin transacción, el insert no se confirma.
@Component
@Transactional
public class ProcessedEventRegistry {

    private final JdbcTemplate jdbc;

    public ProcessedEventRegistry(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public boolean wasProcessed(String eventId) {
        Integer count = jdbc.queryForObject("select count(*) from processed_catalog_event where event_id = ?", Integer.class, eventId);
        return count != null && count > 0;
    }

    public void register(String eventId) {
        jdbc.update("insert into processed_catalog_event (event_id, processed_at) values (?, ?)", eventId, Timestamp.from(Instant.now()));
    }
}

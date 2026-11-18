package ar.edu.um.turnos.catalog.sync.infrastructure.redis;

import ar.edu.um.turnos.catalog.sync.domain.CatalogChange;
import ar.edu.um.turnos.catalog.sync.domain.CatalogSnapshot;
import ar.edu.um.turnos.catalog.sync.domain.CatalogVersions;
import ar.edu.um.turnos.catalog.sync.infrastructure.catedra.CatalogSnapshotResponse.CategoryItem;
import ar.edu.um.turnos.catalog.sync.infrastructure.catedra.CatalogSnapshotResponse.ProfessionalItem;
import ar.edu.um.turnos.catalog.sync.infrastructure.catedra.CatalogSnapshotResponse.ScheduleItem;
import ar.edu.um.turnos.catalog.sync.ports.out.CatalogChangeSource;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Component
public class RedisCatalogChangeSource implements CatalogChangeSource {

    // Claves que publica la cátedra (sección 14 de la referencia de integración)
    private static final String CURRENT_VERSION_KEY = "catedra:sync:current-version";
    private static final String OLDEST_VERSION_KEY = "catedra:sync:oldest-available-version";
    private static final String CHANGES_KEY = "catedra:sync:changes:%d";
    private static final String CATEGORIES_HASH = "catedra:sync:professional-categories";
    private static final String PROFESSIONALS_HASH = "catedra:sync:professionals";
    private static final String SCHEDULES_HASH = "catedra:sync:weekly-schedules";

    private final StringRedisTemplate redis;
    private final JsonMapper json = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();

    public RedisCatalogChangeSource(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public CatalogVersions versions() {
        String current = redis.opsForValue().get(CURRENT_VERSION_KEY);
        String oldest = redis.opsForValue().get(OLDEST_VERSION_KEY);
        if (current == null || oldest == null) {
            throw new IllegalStateException("Redis no tiene las versiones del catálogo");
        }
        return new CatalogVersions(Long.parseLong(current), Long.parseLong(oldest));
    }

    @Override
    public CatalogChange fetchChange(long version) {
        String raw = redis.opsForValue().get(CHANGES_KEY.formatted(version));
        if (raw == null) {
            throw new IllegalStateException("No está disponible el cambio de la versión %d".formatted(version));
        }
        ChangeJson change = json.readValue(raw, ChangeJson.class);

        List<CatalogSnapshot.Category> categories = new ArrayList<>();
        for (String item : readHash(CATEGORIES_HASH, change.changes().professionalCategories())) {
            categories.add(json.readValue(item, CategoryItem.class).toDomain());
        }

        List<CatalogSnapshot.Professional> professionals = new ArrayList<>();
        for (String item : readHash(PROFESSIONALS_HASH, change.changes().professionals())) {
            professionals.add(json.readValue(item, ProfessionalItem.class).toDomain());
        }

        List<CatalogSnapshot.Schedule> schedules = new ArrayList<>();
        for (String item : readHash(SCHEDULES_HASH, change.changes().weeklySchedules())) {
            schedules.add(json.readValue(item, ScheduleItem.class).toDomain());
        }

        return new CatalogChange(version, categories, professionals, schedules);
    }

    // Lee el estado actual de cada id desde el hash de la entidad.
    private List<String> readHash(String hash, List<Long> ids) {
        List<String> values = new ArrayList<>();
        for (Long id : ids) {
            Object value = redis.opsForHash().get(hash, id.toString());
            if (value == null) {
                throw new IllegalStateException("Falta el id %d en %s".formatted(id, hash));
            }
            values.add(value.toString());
        }
        return values;
    }

    record ChangeJson(Long version, Ids changes) {}

    record Ids(List<Long> professionalCategories, List<Long> professionals, List<Long> weeklySchedules) {}
}

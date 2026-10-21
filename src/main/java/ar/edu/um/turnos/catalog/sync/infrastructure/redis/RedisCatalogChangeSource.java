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

    private static final String PREFIX = "catedra:sync:";

    private final StringRedisTemplate redis;
    private final JsonMapper json = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();

    public RedisCatalogChangeSource(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public CatalogVersions versions() {
        String current = redis.opsForValue().get(PREFIX + "current-version");
        String oldest = redis.opsForValue().get(PREFIX + "oldest-available-version");
        if (current == null || oldest == null) {
            throw new IllegalStateException("Redis no tiene las versiones del catálogo");
        }
        return new CatalogVersions(Long.parseLong(current), Long.parseLong(oldest));
    }

    @Override
    public CatalogChange fetchChange(long version) {
        String raw = redis.opsForValue().get(PREFIX + "changes:" + version);
        if (raw == null) {
            throw new IllegalStateException("No está disponible el cambio de la versión " + version);
        }
        ChangeJson change = json.readValue(raw, ChangeJson.class);

        List<CatalogSnapshot.Category> categories = new ArrayList<>();
        for (String item : readHash("professional-categories", change.changes().professionalCategories())) {
            categories.add(json.readValue(item, CategoryItem.class).toDomain());
        }

        List<CatalogSnapshot.Professional> professionals = new ArrayList<>();
        for (String item : readHash("professionals", change.changes().professionals())) {
            professionals.add(json.readValue(item, ProfessionalItem.class).toDomain());
        }

        List<CatalogSnapshot.Schedule> schedules = new ArrayList<>();
        for (String item : readHash("weekly-schedules", change.changes().weeklySchedules())) {
            schedules.add(json.readValue(item, ScheduleItem.class).toDomain());
        }

        return new CatalogChange(version, categories, professionals, schedules);
    }

    // Lee el estado actual de cada id desde el hash de la entidad.
    private List<String> readHash(String hash, List<Long> ids) {
        List<String> values = new ArrayList<>();
        for (Long id : ids) {
            Object value = redis.opsForHash().get(PREFIX + hash, id.toString());
            if (value == null) {
                throw new IllegalStateException("Falta el id " + id + " en " + hash);
            }
            values.add(value.toString());
        }
        return values;
    }

    record ChangeJson(Long version, Ids changes) {}

    record Ids(List<Long> professionalCategories, List<Long> professionals, List<Long> weeklySchedules) {}
}

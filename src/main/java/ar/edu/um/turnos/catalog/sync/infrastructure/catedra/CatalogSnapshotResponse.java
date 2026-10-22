package ar.edu.um.turnos.catalog.sync.infrastructure.catedra;

import ar.edu.um.turnos.catalog.sync.domain.CatalogSnapshot;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// JSON de GET /api/synchronization/snapshot. Los ítems tienen el mismo formato que en Redis.
public record CatalogSnapshotResponse(
    Long snapshotVersion,
    List<CategoryItem> professionalCategories,
    List<ProfessionalItem> professionals,
    List<ScheduleItem> weeklySchedules
) {
    public CatalogSnapshot toDomain() {
        List<CatalogSnapshot.Category> categories = new ArrayList<>();
        for (CategoryItem c : professionalCategories) {
            categories.add(c.toDomain());
        }

        List<CatalogSnapshot.Professional> professionalList = new ArrayList<>();
        for (ProfessionalItem p : professionals) {
            professionalList.add(p.toDomain());
        }

        List<CatalogSnapshot.Schedule> schedules = new ArrayList<>();
        for (ScheduleItem s : weeklySchedules) {
            schedules.add(s.toDomain());
        }

        return new CatalogSnapshot(snapshotVersion, categories, professionalList, schedules);
    }

    public record CategoryItem(Long id, String name, String description, Boolean enabled, Instant createdAt, Instant updatedAt) {
        public CatalogSnapshot.Category toDomain() {
            return new CatalogSnapshot.Category(id, name, description, enabled, createdAt, updatedAt);
        }
    }

    public record ProfessionalItem(
        Long id,
        Long categoryId,
        String firstName,
        String lastName,
        Boolean enabled,
        Instant createdAt,
        Instant updatedAt
    ) {
        public CatalogSnapshot.Professional toDomain() {
            return new CatalogSnapshot.Professional(id, categoryId, firstName, lastName, enabled, createdAt, updatedAt);
        }
    }

    public record ScheduleItem(
        Long id,
        Long professionalId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        Integer slotDurationMinutes,
        Boolean enabled,
        Instant createdAt,
        Instant updatedAt
    ) {
        public CatalogSnapshot.Schedule toDomain() {
            return new CatalogSnapshot.Schedule(
                id,
                professionalId,
                dayOfWeek,
                startTime,
                endTime,
                slotDurationMinutes,
                enabled,
                createdAt,
                updatedAt
            );
        }
    }
}

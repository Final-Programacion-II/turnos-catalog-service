package ar.edu.um.turnos.catalog.sync.infrastructure.catedra;

import ar.edu.um.turnos.catalog.sync.domain.CatalogSnapshot;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// JSON de GET /api/synchronization/snapshot
public record CatalogSnapshotResponse(
    Long snapshotVersion,
    List<CategoryItem> professionalCategories,
    List<ProfessionalItem> professionals,
    List<ScheduleItem> weeklySchedules
) {
    public CatalogSnapshot toDomain() {
        List<CatalogSnapshot.Category> categories = new ArrayList<>();
        for (CategoryItem c : professionalCategories) {
            categories.add(new CatalogSnapshot.Category(c.id(), c.name(), c.description(), c.enabled(), c.createdAt(), c.updatedAt()));
        }

        List<CatalogSnapshot.Professional> professionalList = new ArrayList<>();
        for (ProfessionalItem p : professionals) {
            professionalList.add(
                new CatalogSnapshot.Professional(
                    p.id(),
                    p.categoryId(),
                    p.firstName(),
                    p.lastName(),
                    p.enabled(),
                    p.createdAt(),
                    p.updatedAt()
                )
            );
        }

        List<CatalogSnapshot.Schedule> schedules = new ArrayList<>();
        for (ScheduleItem s : weeklySchedules) {
            schedules.add(
                new CatalogSnapshot.Schedule(
                    s.id(),
                    s.professionalId(),
                    s.dayOfWeek(),
                    s.startTime(),
                    s.endTime(),
                    s.slotDurationMinutes(),
                    s.enabled(),
                    s.createdAt(),
                    s.updatedAt()
                )
            );
        }

        return new CatalogSnapshot(snapshotVersion, categories, professionalList, schedules);
    }

    public record CategoryItem(Long id, String name, String description, Boolean enabled, Instant createdAt, Instant updatedAt) {}

    public record ProfessionalItem(
        Long id,
        Long categoryId,
        String firstName,
        String lastName,
        Boolean enabled,
        Instant createdAt,
        Instant updatedAt
    ) {}

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
    ) {}
}

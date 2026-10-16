package ar.edu.um.turnos.catalog.sync.domain;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record CatalogSnapshot(Long version, List<Category> categories, List<Professional> professionals, List<Schedule> schedules) {
    public CatalogSnapshot {
        if (version == null) {
            throw new IllegalArgumentException("El snapshot no tiene versión");
        }
    }

    public void validateReferences() {
        Set<Long> categoryIds = new HashSet<>();
        for (Category c : categories) {
            categoryIds.add(c.id());
        }

        Set<Long> professionalIds = new HashSet<>();
        for (Professional p : professionals) {
            if (!categoryIds.contains(p.categoryId())) {
                throw new IllegalArgumentException("El profesional " + p.id() + " tiene una categoría inexistente: " + p.categoryId());
            }
            professionalIds.add(p.id());
        }

        for (Schedule s : schedules) {
            if (!professionalIds.contains(s.professionalId())) {
                throw new IllegalArgumentException("El horario " + s.id() + " tiene un profesional inexistente: " + s.professionalId());
            }
        }
    }

    public record Category(Long id, String name, String description, Boolean enabled, Instant createdAt, Instant updatedAt) {}

    public record Professional(
        Long id,
        Long categoryId,
        String firstName,
        String lastName,
        Boolean enabled,
        Instant createdAt,
        Instant updatedAt
    ) {}

    public record Schedule(
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

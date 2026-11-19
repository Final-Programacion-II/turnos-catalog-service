package ar.edu.um.turnos.catalog.service.dto;

import ar.edu.um.turnos.catalog.repository.entity.WeeklySchedule;
import ar.edu.um.turnos.catalog.repository.entity.enumeration.Weekday;
import java.time.Instant;
import java.time.LocalTime;

/**
 * Un horario semanal de la copia local, tal como lo devuelve la API.
 */
public record WeeklyScheduleDTO(
    Long id,
    Long externalId,
    Weekday dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    Integer slotDurationMinutes,
    Boolean enabled,
    Instant createdAt,
    Instant updatedAt,
    ProfessionalRef professional
) {
    public static WeeklyScheduleDTO from(WeeklySchedule schedule) {
        return new WeeklyScheduleDTO(
            schedule.getId(),
            schedule.getExternalId(),
            schedule.getDayOfWeek(),
            schedule.getStartTime(),
            schedule.getEndTime(),
            schedule.getSlotDurationMinutes(),
            schedule.getEnabled(),
            schedule.getCreatedAt(),
            schedule.getUpdatedAt(),
            schedule.getProfessional() == null ? null : new ProfessionalRef(schedule.getProfessional().getId())
        );
    }

    /**
     * Del profesional va solo el id: con él se pide el resto en /api/professionals/{id}.
     */
    public record ProfessionalRef(Long id) {}
}

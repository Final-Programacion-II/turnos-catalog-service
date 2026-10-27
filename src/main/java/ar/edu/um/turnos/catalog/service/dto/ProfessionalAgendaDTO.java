package ar.edu.um.turnos.catalog.service.dto;

import ar.edu.um.turnos.catalog.domain.enumeration.Weekday;
import java.time.LocalTime;
import java.util.List;

/**
 * Agenda vigente de un profesional, tal como la consume el servicio de turnos.
 * bookable es false si el profesional o su categoría están dados de baja: en ese caso no se pueden pedir turnos.
 * Los horarios son solo los habilitados, ordenados por día y hora de inicio.
 */
public record ProfessionalAgendaDTO(
    Long id,
    Long externalId,
    String firstName,
    String lastName,
    String categoryName,
    boolean bookable,
    Long catalogVersion,
    List<Schedule> schedules
) {
    public record Schedule(Long externalId, Weekday dayOfWeek, LocalTime startTime, LocalTime endTime, Integer slotDurationMinutes) {}
}

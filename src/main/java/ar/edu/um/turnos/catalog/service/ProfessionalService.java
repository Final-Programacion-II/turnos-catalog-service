package ar.edu.um.turnos.catalog.service;

import ar.edu.um.turnos.catalog.domain.Professional;
import ar.edu.um.turnos.catalog.domain.enumeration.Weekday;
import ar.edu.um.turnos.catalog.repository.ProfessionalRepository;
import ar.edu.um.turnos.catalog.repository.WeeklyScheduleRepository;
import ar.edu.um.turnos.catalog.service.dto.ProfessionalDTO;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta de profesionales sobre la copia local. Es de solo lectura: los profesionales se
 * crean y se modifican solo con la sincronización.
 */
@Service
@Transactional(readOnly = true)
public class ProfessionalService {

    private static final Logger LOG = LoggerFactory.getLogger(ProfessionalService.class);

    private final ProfessionalRepository professionalRepository;

    private final WeeklyScheduleRepository weeklyScheduleRepository;

    public ProfessionalService(ProfessionalRepository professionalRepository, WeeklyScheduleRepository weeklyScheduleRepository) {
        this.professionalRepository = professionalRepository;
        this.weeklyScheduleRepository = weeklyScheduleRepository;
    }

    /**
     * Todos los profesionales, habilitados o no, con su categoría.
     */
    public List<ProfessionalDTO> findAll() {
        return professionalRepository.findAllWithEagerRelationships().stream().map(ProfessionalDTO::from).toList();
    }

    /**
     * Busca profesionales en la copia local. Los filtros en null no se aplican.
     * "Habilitado" significa que el profesional y su categoría están habilitados.
     * La disponibilidad se cumple si tiene algún horario habilitado ese día que se superponga con la franja.
     */
    public List<ProfessionalDTO> search(Long categoryId, String name, Boolean enabled, Weekday day, LocalTime fromTime, LocalTime toTime) {
        LOG.debug(
            "Request to search Professionals : categoryId={}, name={}, enabled={}, day={}, from={}, to={}",
            categoryId,
            name,
            enabled,
            day,
            fromTime,
            toTime
        );
        // Se arma el patrón acá: si se manda un String null, PostgreSQL no sabe de qué tipo es y falla el lower()
        String namePattern = name == null || name.isBlank() ? "%" : "%" + name.trim().toLowerCase(Locale.ROOT) + "%";
        List<Professional> professionals = professionalRepository.search(categoryId, namePattern, enabled);

        if (day != null || fromTime != null || toTime != null) {
            Set<Long> attending = professionalsAttending(day, fromTime, toTime);
            professionals = professionals
                .stream()
                .filter(professional -> attending.contains(professional.getId()))
                .toList();
        }
        return professionals.stream().map(ProfessionalDTO::from).toList();
    }

    /**
     * Ids de los profesionales con algún horario habilitado que cumple el día y se superpone con la franja.
     * Las horas se comparan en Java y no en la consulta: así no dependen de cómo el driver manda un LocalTime.
     */
    private Set<Long> professionalsAttending(Weekday day, LocalTime fromTime, LocalTime toTime) {
        return weeklyScheduleRepository
            .findAllByEnabledTrue()
            .stream()
            .filter(schedule -> day == null || schedule.getDayOfWeek() == day)
            .filter(schedule -> fromTime == null || schedule.getEndTime().isAfter(fromTime))
            .filter(schedule -> toTime == null || schedule.getStartTime().isBefore(toTime))
            .map(schedule -> schedule.getProfessional().getId())
            .collect(Collectors.toSet());
    }

    public Optional<ProfessionalDTO> findOne(Long id) {
        return professionalRepository.findOneWithEagerRelationships(id).map(ProfessionalDTO::from);
    }
}

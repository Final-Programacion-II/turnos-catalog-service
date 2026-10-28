package ar.edu.um.turnos.catalog.service;

import ar.edu.um.turnos.catalog.domain.CatalogSyncState;
import ar.edu.um.turnos.catalog.domain.Professional;
import ar.edu.um.turnos.catalog.domain.WeeklySchedule;
import ar.edu.um.turnos.catalog.repository.CatalogSyncStateRepository;
import ar.edu.um.turnos.catalog.repository.ProfessionalRepository;
import ar.edu.um.turnos.catalog.repository.WeeklyScheduleRepository;
import ar.edu.um.turnos.catalog.service.dto.ProfessionalAgendaDTO;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Arma la agenda vigente de un profesional a partir de la copia local del catálogo.
 */
@Service
@Transactional(readOnly = true)
public class ProfessionalAgendaService {

    private final ProfessionalRepository professionalRepository;

    private final WeeklyScheduleRepository weeklyScheduleRepository;

    private final CatalogSyncStateRepository catalogSyncStateRepository;

    public ProfessionalAgendaService(
        ProfessionalRepository professionalRepository,
        WeeklyScheduleRepository weeklyScheduleRepository,
        CatalogSyncStateRepository catalogSyncStateRepository
    ) {
        this.professionalRepository = professionalRepository;
        this.weeklyScheduleRepository = weeklyScheduleRepository;
        this.catalogSyncStateRepository = catalogSyncStateRepository;
    }

    public Optional<ProfessionalAgendaDTO> findAgenda(Long professionalId) {
        return professionalRepository.findOneWithEagerRelationships(professionalId).map(this::toAgenda);
    }

    private ProfessionalAgendaDTO toAgenda(Professional professional) {
        List<ProfessionalAgendaDTO.Schedule> schedules = weeklyScheduleRepository
            .findAllByProfessionalIdAndEnabledTrue(professional.getId())
            .stream()
            // El enum ordena de lunes a domingo; en la base el día está guardado como texto y ordenaría alfabéticamente
            .sorted(Comparator.comparing(WeeklySchedule::getDayOfWeek).thenComparing(WeeklySchedule::getStartTime))
            .map(schedule ->
                new ProfessionalAgendaDTO.Schedule(
                    schedule.getExternalId(),
                    schedule.getDayOfWeek(),
                    schedule.getStartTime(),
                    schedule.getEndTime(),
                    schedule.getSlotDurationMinutes()
                )
            )
            .toList();

        boolean bookable = Boolean.TRUE.equals(professional.getEnabled()) && Boolean.TRUE.equals(professional.getCategory().getEnabled());

        Long catalogVersion = catalogSyncStateRepository
            .findById(CatalogSyncState.SINGLETON_ID)
            .map(CatalogSyncState::getCatalogVersion)
            .orElse(null);

        return new ProfessionalAgendaDTO(
            professional.getId(),
            professional.getExternalId(),
            professional.getFirstName(),
            professional.getLastName(),
            professional.getCategory().getName(),
            bookable,
            catalogVersion,
            schedules
        );
    }
}

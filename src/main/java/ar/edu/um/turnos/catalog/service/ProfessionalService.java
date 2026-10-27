package ar.edu.um.turnos.catalog.service;

import ar.edu.um.turnos.catalog.domain.Professional;
import ar.edu.um.turnos.catalog.domain.enumeration.Weekday;
import ar.edu.um.turnos.catalog.repository.ProfessionalRepository;
import ar.edu.um.turnos.catalog.repository.WeeklyScheduleRepository;
import ar.edu.um.turnos.catalog.service.dto.ProfessionalDTO;
import ar.edu.um.turnos.catalog.service.mapper.ProfessionalMapper;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ar.edu.um.turnos.catalog.domain.Professional}.
 */
@Service
@Transactional
public class ProfessionalService {

    private static final Logger LOG = LoggerFactory.getLogger(ProfessionalService.class);

    private final ProfessionalRepository professionalRepository;

    private final ProfessionalMapper professionalMapper;

    private final WeeklyScheduleRepository weeklyScheduleRepository;

    public ProfessionalService(
        ProfessionalRepository professionalRepository,
        ProfessionalMapper professionalMapper,
        WeeklyScheduleRepository weeklyScheduleRepository
    ) {
        this.professionalRepository = professionalRepository;
        this.professionalMapper = professionalMapper;
        this.weeklyScheduleRepository = weeklyScheduleRepository;
    }

    /**
     * Save a professional.
     *
     * @param professionalDTO the entity to save.
     * @return the persisted entity.
     */
    public ProfessionalDTO save(ProfessionalDTO professionalDTO) {
        LOG.debug("Request to save Professional : {}", professionalDTO);
        Professional professional = professionalMapper.toEntity(professionalDTO);
        professional = professionalRepository.save(professional);
        return professionalMapper.toDto(professional);
    }

    /**
     * Update a professional.
     *
     * @param professionalDTO the entity to save.
     * @return the persisted entity.
     */
    public ProfessionalDTO update(ProfessionalDTO professionalDTO) {
        LOG.debug("Request to update Professional : {}", professionalDTO);
        Professional professional = professionalMapper.toEntity(professionalDTO);
        professional = professionalRepository.save(professional);
        return professionalMapper.toDto(professional);
    }

    /**
     * Partially update a professional.
     *
     * @param professionalDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ProfessionalDTO> partialUpdate(ProfessionalDTO professionalDTO) {
        LOG.debug("Request to partially update Professional : {}", professionalDTO);

        return professionalRepository
            .findById(professionalDTO.getId())
            .map(existingProfessional -> {
                professionalMapper.partialUpdate(existingProfessional, professionalDTO);

                return existingProfessional;
            })
            .map(professionalRepository::save)
            .map(professionalMapper::toDto);
    }

    /**
     * Get all the professionals.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<ProfessionalDTO> findAll() {
        LOG.debug("Request to get all Professionals");
        return professionalRepository.findAll().stream().map(professionalMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Busca profesionales en la copia local. Los filtros en null no se aplican.
     * "Habilitado" significa que el profesional y su categoría están habilitados.
     * La disponibilidad se cumple si tiene algún horario habilitado ese día que se superponga con la franja.
     */
    @Transactional(readOnly = true)
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
        return professionals.stream().map(professionalMapper::toDto).toList();
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

    /**
     * Get all the professionals with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ProfessionalDTO> findAllWithEagerRelationships(Pageable pageable) {
        return professionalRepository.findAllWithEagerRelationships(pageable).map(professionalMapper::toDto);
    }

    /**
     * Get one professional by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ProfessionalDTO> findOne(Long id) {
        LOG.debug("Request to get Professional : {}", id);
        return professionalRepository.findOneWithEagerRelationships(id).map(professionalMapper::toDto);
    }

    /**
     * Delete the professional by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Professional : {}", id);
        professionalRepository.deleteById(id);
    }
}

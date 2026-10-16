package ar.edu.um.turnos.catalog.service;

import ar.edu.um.turnos.catalog.domain.WeeklySchedule;
import ar.edu.um.turnos.catalog.repository.WeeklyScheduleRepository;
import ar.edu.um.turnos.catalog.service.dto.WeeklyScheduleDTO;
import ar.edu.um.turnos.catalog.service.mapper.WeeklyScheduleMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ar.edu.um.turnos.catalog.domain.WeeklySchedule}.
 */
@Service
@Transactional
public class WeeklyScheduleService {

    private static final Logger LOG = LoggerFactory.getLogger(WeeklyScheduleService.class);

    private final WeeklyScheduleRepository weeklyScheduleRepository;

    private final WeeklyScheduleMapper weeklyScheduleMapper;

    public WeeklyScheduleService(WeeklyScheduleRepository weeklyScheduleRepository, WeeklyScheduleMapper weeklyScheduleMapper) {
        this.weeklyScheduleRepository = weeklyScheduleRepository;
        this.weeklyScheduleMapper = weeklyScheduleMapper;
    }

    /**
     * Save a weeklySchedule.
     *
     * @param weeklyScheduleDTO the entity to save.
     * @return the persisted entity.
     */
    public WeeklyScheduleDTO save(WeeklyScheduleDTO weeklyScheduleDTO) {
        LOG.debug("Request to save WeeklySchedule : {}", weeklyScheduleDTO);
        WeeklySchedule weeklySchedule = weeklyScheduleMapper.toEntity(weeklyScheduleDTO);
        weeklySchedule = weeklyScheduleRepository.save(weeklySchedule);
        return weeklyScheduleMapper.toDto(weeklySchedule);
    }

    /**
     * Update a weeklySchedule.
     *
     * @param weeklyScheduleDTO the entity to save.
     * @return the persisted entity.
     */
    public WeeklyScheduleDTO update(WeeklyScheduleDTO weeklyScheduleDTO) {
        LOG.debug("Request to update WeeklySchedule : {}", weeklyScheduleDTO);
        WeeklySchedule weeklySchedule = weeklyScheduleMapper.toEntity(weeklyScheduleDTO);
        weeklySchedule = weeklyScheduleRepository.save(weeklySchedule);
        return weeklyScheduleMapper.toDto(weeklySchedule);
    }

    /**
     * Partially update a weeklySchedule.
     *
     * @param weeklyScheduleDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<WeeklyScheduleDTO> partialUpdate(WeeklyScheduleDTO weeklyScheduleDTO) {
        LOG.debug("Request to partially update WeeklySchedule : {}", weeklyScheduleDTO);

        return weeklyScheduleRepository
            .findById(weeklyScheduleDTO.getId())
            .map(existingWeeklySchedule -> {
                weeklyScheduleMapper.partialUpdate(existingWeeklySchedule, weeklyScheduleDTO);

                return existingWeeklySchedule;
            })
            .map(weeklyScheduleRepository::save)
            .map(weeklyScheduleMapper::toDto);
    }

    /**
     * Get all the weeklySchedules.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<WeeklyScheduleDTO> findAll() {
        LOG.debug("Request to get all WeeklySchedules");
        return weeklyScheduleRepository
            .findAll()
            .stream()
            .map(weeklyScheduleMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one weeklySchedule by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<WeeklyScheduleDTO> findOne(Long id) {
        LOG.debug("Request to get WeeklySchedule : {}", id);
        return weeklyScheduleRepository.findById(id).map(weeklyScheduleMapper::toDto);
    }

    /**
     * Delete the weeklySchedule by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete WeeklySchedule : {}", id);
        weeklyScheduleRepository.deleteById(id);
    }
}

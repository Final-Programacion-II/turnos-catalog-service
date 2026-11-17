package ar.edu.um.turnos.catalog.service;

import ar.edu.um.turnos.catalog.repository.WeeklyScheduleRepository;
import ar.edu.um.turnos.catalog.service.dto.WeeklyScheduleDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta de horarios semanales sobre la copia local. Es de solo lectura: los horarios se
 * crean y se modifican solo con la sincronización.
 */
@Service
@Transactional(readOnly = true)
public class WeeklyScheduleService {

    private final WeeklyScheduleRepository weeklyScheduleRepository;

    public WeeklyScheduleService(WeeklyScheduleRepository weeklyScheduleRepository) {
        this.weeklyScheduleRepository = weeklyScheduleRepository;
    }

    public List<WeeklyScheduleDTO> findAll() {
        return weeklyScheduleRepository.findAll().stream().map(WeeklyScheduleDTO::from).toList();
    }

    public Optional<WeeklyScheduleDTO> findOne(Long id) {
        return weeklyScheduleRepository.findById(id).map(WeeklyScheduleDTO::from);
    }
}

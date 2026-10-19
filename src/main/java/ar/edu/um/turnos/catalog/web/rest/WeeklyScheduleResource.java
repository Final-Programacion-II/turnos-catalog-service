package ar.edu.um.turnos.catalog.web.rest;

import ar.edu.um.turnos.catalog.service.WeeklyScheduleService;
import ar.edu.um.turnos.catalog.service.dto.WeeklyScheduleDTO;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link ar.edu.um.turnos.catalog.domain.WeeklySchedule}.
 */
@RestController
@RequestMapping("/api/weekly-schedules")
public class WeeklyScheduleResource {

    private static final Logger LOG = LoggerFactory.getLogger(WeeklyScheduleResource.class);

    private final WeeklyScheduleService weeklyScheduleService;

    public WeeklyScheduleResource(WeeklyScheduleService weeklyScheduleService) {
        this.weeklyScheduleService = weeklyScheduleService;
    }

    /**
     * {@code GET  /weekly-schedules} : get all the Weekly Schedules.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Weekly Schedules in body.
     */
    @GetMapping("")
    public List<WeeklyScheduleDTO> getAllWeeklySchedules() {
        LOG.debug("REST request to get all WeeklySchedules");
        return weeklyScheduleService.findAll();
    }

    /**
     * {@code GET  /weekly-schedules/:id} : get the "id" weeklySchedule.
     *
     * @param id the id of the weeklyScheduleDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the weeklyScheduleDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<WeeklyScheduleDTO> getWeeklySchedule(@PathVariable("id") Long id) {
        LOG.debug("REST request to get WeeklySchedule : {}", id);
        Optional<WeeklyScheduleDTO> weeklyScheduleDTO = weeklyScheduleService.findOne(id);
        return ResponseUtil.wrapOrNotFound(weeklyScheduleDTO);
    }
}

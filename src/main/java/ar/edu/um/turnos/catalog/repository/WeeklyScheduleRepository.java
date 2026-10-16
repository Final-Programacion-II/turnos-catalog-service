package ar.edu.um.turnos.catalog.repository;

import ar.edu.um.turnos.catalog.domain.WeeklySchedule;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the WeeklySchedule entity.
 */
@SuppressWarnings("unused")
@Repository
public interface WeeklyScheduleRepository extends JpaRepository<WeeklySchedule, Long> {}

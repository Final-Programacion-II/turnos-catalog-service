package ar.edu.um.turnos.catalog.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.um.turnos.catalog.domain.Professional;
import ar.edu.um.turnos.catalog.domain.WeeklySchedule;
import ar.edu.um.turnos.catalog.domain.enumeration.Weekday;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class WeeklyScheduleDTOTest {

    @Test
    void copiaElHorarioYDelProfesionalSoloElId() {
        Professional profesional = new Professional();
        profesional.setId(1067L);
        profesional.setFirstName("Romina");

        WeeklySchedule horario = new WeeklySchedule();
        horario.setId(1152L);
        horario.setExternalId(332L);
        horario.setDayOfWeek(Weekday.TUESDAY);
        horario.setStartTime(LocalTime.of(13, 0));
        horario.setEndTime(LocalTime.of(16, 0));
        horario.setSlotDurationMinutes(30);
        horario.setEnabled(true);
        horario.setProfessional(profesional);

        WeeklyScheduleDTO dto = WeeklyScheduleDTO.from(horario);

        assertThat(dto.id()).isEqualTo(1152L);
        assertThat(dto.externalId()).isEqualTo(332L);
        assertThat(dto.dayOfWeek()).isEqualTo(Weekday.TUESDAY);
        assertThat(dto.startTime()).isEqualTo(LocalTime.of(13, 0));
        assertThat(dto.endTime()).isEqualTo(LocalTime.of(16, 0));
        assertThat(dto.slotDurationMinutes()).isEqualTo(30);
        assertThat(dto.enabled()).isTrue();
        assertThat(dto.professional()).isEqualTo(new WeeklyScheduleDTO.ProfessionalRef(1067L));
    }

    @Test
    void sinProfesionalNoFalla() {
        assertThat(WeeklyScheduleDTO.from(new WeeklySchedule()).professional()).isNull();
    }
}

package ar.edu.um.turnos.catalog.repository.entity;

import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.um.turnos.catalog.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class WeeklyScheduleTest {

    @Test
    void dosEntidadesSonIgualesSiTienenElMismoId() throws Exception {
        TestUtil.equalsVerifier(WeeklySchedule.class);

        WeeklySchedule horario1 = new WeeklySchedule();
        horario1.setId(1L);
        WeeklySchedule horario2 = new WeeklySchedule();
        assertThat(horario1).isNotEqualTo(horario2);

        horario2.setId(1L);
        assertThat(horario1).isEqualTo(horario2);

        horario2.setId(2L);
        assertThat(horario1).isNotEqualTo(horario2);
    }

    @Test
    void guardaSuProfesional() {
        Professional profesional = new Professional();
        profesional.setId(7L);
        WeeklySchedule horario = new WeeklySchedule();

        horario.setProfessional(profesional);
        assertThat(horario.getProfessional()).isEqualTo(profesional);

        horario.setProfessional(null);
        assertThat(horario.getProfessional()).isNull();
    }
}

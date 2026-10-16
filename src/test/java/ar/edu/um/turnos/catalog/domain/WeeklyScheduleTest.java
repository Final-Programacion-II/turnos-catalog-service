package ar.edu.um.turnos.catalog.domain;

import static ar.edu.um.turnos.catalog.domain.ProfessionalTestSamples.*;
import static ar.edu.um.turnos.catalog.domain.WeeklyScheduleTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.um.turnos.catalog.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class WeeklyScheduleTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(WeeklySchedule.class);
        WeeklySchedule weeklySchedule1 = getWeeklyScheduleSample1();
        WeeklySchedule weeklySchedule2 = new WeeklySchedule();
        assertThat(weeklySchedule1).isNotEqualTo(weeklySchedule2);

        weeklySchedule2.setId(weeklySchedule1.getId());
        assertThat(weeklySchedule1).isEqualTo(weeklySchedule2);

        weeklySchedule2 = getWeeklyScheduleSample2();
        assertThat(weeklySchedule1).isNotEqualTo(weeklySchedule2);
    }

    @Test
    void professionalTest() {
        WeeklySchedule weeklySchedule = getWeeklyScheduleRandomSampleGenerator();
        Professional professionalBack = getProfessionalRandomSampleGenerator();

        weeklySchedule.setProfessional(professionalBack);
        assertThat(weeklySchedule.getProfessional()).isEqualTo(professionalBack);

        weeklySchedule.professional(null);
        assertThat(weeklySchedule.getProfessional()).isNull();
    }
}

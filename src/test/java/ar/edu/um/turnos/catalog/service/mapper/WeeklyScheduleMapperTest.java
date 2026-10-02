package ar.edu.um.turnos.catalog.service.mapper;

import static ar.edu.um.turnos.catalog.domain.WeeklyScheduleAsserts.*;
import static ar.edu.um.turnos.catalog.domain.WeeklyScheduleTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WeeklyScheduleMapperTest {

    private WeeklyScheduleMapper weeklyScheduleMapper;

    @BeforeEach
    void setUp() {
        weeklyScheduleMapper = new WeeklyScheduleMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getWeeklyScheduleSample1();
        var actual = weeklyScheduleMapper.toEntity(weeklyScheduleMapper.toDto(expected));
        assertWeeklyScheduleAllPropertiesEquals(expected, actual);
    }
}

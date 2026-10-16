package ar.edu.um.turnos.catalog.service.mapper;

import static ar.edu.um.turnos.catalog.domain.ProfessionalAsserts.*;
import static ar.edu.um.turnos.catalog.domain.ProfessionalTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProfessionalMapperTest {

    private ProfessionalMapper professionalMapper;

    @BeforeEach
    void setUp() {
        professionalMapper = new ProfessionalMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getProfessionalSample1();
        var actual = professionalMapper.toEntity(professionalMapper.toDto(expected));
        assertProfessionalAllPropertiesEquals(expected, actual);
    }
}

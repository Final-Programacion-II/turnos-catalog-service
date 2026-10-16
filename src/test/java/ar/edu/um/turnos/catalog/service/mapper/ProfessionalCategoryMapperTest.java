package ar.edu.um.turnos.catalog.service.mapper;

import static ar.edu.um.turnos.catalog.domain.ProfessionalCategoryAsserts.*;
import static ar.edu.um.turnos.catalog.domain.ProfessionalCategoryTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProfessionalCategoryMapperTest {

    private ProfessionalCategoryMapper professionalCategoryMapper;

    @BeforeEach
    void setUp() {
        professionalCategoryMapper = new ProfessionalCategoryMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getProfessionalCategorySample1();
        var actual = professionalCategoryMapper.toEntity(professionalCategoryMapper.toDto(expected));
        assertProfessionalCategoryAllPropertiesEquals(expected, actual);
    }
}

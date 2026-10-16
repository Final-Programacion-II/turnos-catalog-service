package ar.edu.um.turnos.catalog.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.um.turnos.catalog.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ProfessionalCategoryDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProfessionalCategoryDTO.class);
        ProfessionalCategoryDTO professionalCategoryDTO1 = new ProfessionalCategoryDTO();
        professionalCategoryDTO1.setId(1L);
        ProfessionalCategoryDTO professionalCategoryDTO2 = new ProfessionalCategoryDTO();
        assertThat(professionalCategoryDTO1).isNotEqualTo(professionalCategoryDTO2);
        professionalCategoryDTO2.setId(professionalCategoryDTO1.getId());
        assertThat(professionalCategoryDTO1).isEqualTo(professionalCategoryDTO2);
        professionalCategoryDTO2.setId(2L);
        assertThat(professionalCategoryDTO1).isNotEqualTo(professionalCategoryDTO2);
        professionalCategoryDTO1.setId(null);
        assertThat(professionalCategoryDTO1).isNotEqualTo(professionalCategoryDTO2);
    }
}

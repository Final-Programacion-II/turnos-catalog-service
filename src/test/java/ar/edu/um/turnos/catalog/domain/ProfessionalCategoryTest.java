package ar.edu.um.turnos.catalog.domain;

import static ar.edu.um.turnos.catalog.domain.ProfessionalCategoryTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.um.turnos.catalog.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ProfessionalCategoryTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProfessionalCategory.class);
        ProfessionalCategory professionalCategory1 = getProfessionalCategorySample1();
        ProfessionalCategory professionalCategory2 = new ProfessionalCategory();
        assertThat(professionalCategory1).isNotEqualTo(professionalCategory2);

        professionalCategory2.setId(professionalCategory1.getId());
        assertThat(professionalCategory1).isEqualTo(professionalCategory2);

        professionalCategory2 = getProfessionalCategorySample2();
        assertThat(professionalCategory1).isNotEqualTo(professionalCategory2);
    }
}

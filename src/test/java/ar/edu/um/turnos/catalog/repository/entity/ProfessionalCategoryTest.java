package ar.edu.um.turnos.catalog.repository.entity;

import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.um.turnos.catalog.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ProfessionalCategoryTest {

    @Test
    void dosEntidadesSonIgualesSiTienenElMismoId() throws Exception {
        TestUtil.equalsVerifier(ProfessionalCategory.class);

        ProfessionalCategory categoria1 = new ProfessionalCategory();
        categoria1.setId(1L);
        ProfessionalCategory categoria2 = new ProfessionalCategory();
        assertThat(categoria1).isNotEqualTo(categoria2);

        categoria2.setId(1L);
        assertThat(categoria1).isEqualTo(categoria2);

        categoria2.setId(2L);
        assertThat(categoria1).isNotEqualTo(categoria2);
    }
}

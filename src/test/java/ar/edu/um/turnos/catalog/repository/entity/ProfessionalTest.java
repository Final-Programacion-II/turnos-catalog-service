package ar.edu.um.turnos.catalog.repository.entity;

import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.um.turnos.catalog.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ProfessionalTest {

    @Test
    void dosEntidadesSonIgualesSiTienenElMismoId() throws Exception {
        TestUtil.equalsVerifier(Professional.class);

        Professional profesional1 = new Professional();
        profesional1.setId(1L);
        Professional profesional2 = new Professional();
        assertThat(profesional1).isNotEqualTo(profesional2);

        profesional2.setId(1L);
        assertThat(profesional1).isEqualTo(profesional2);

        profesional2.setId(2L);
        assertThat(profesional1).isNotEqualTo(profesional2);
    }

    @Test
    void guardaSuCategoria() {
        ProfessionalCategory categoria = new ProfessionalCategory();
        categoria.setId(7L);
        Professional profesional = new Professional();

        profesional.setCategory(categoria);
        assertThat(profesional.getCategory()).isEqualTo(categoria);

        profesional.setCategory(null);
        assertThat(profesional.getCategory()).isNull();
    }
}

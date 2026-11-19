package ar.edu.um.turnos.catalog.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.um.turnos.catalog.repository.entity.Professional;
import ar.edu.um.turnos.catalog.repository.entity.ProfessionalCategory;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ProfessionalDTOTest {

    private static final Instant CREADO = Instant.parse("2026-10-23T14:02:11Z");
    private static final Instant ACTUALIZADO = Instant.parse("2026-11-08T21:00:03Z");

    @Test
    void copiaLosDatosDelProfesionalYDeLaCategoriaSoloIdYNombre() {
        ProfessionalCategory categoria = new ProfessionalCategory();
        categoria.setId(1003L);
        categoria.setExternalId(4L);
        categoria.setName("Ginecologia");
        categoria.setDescription("No viaja en el profesional");

        Professional profesional = new Professional();
        profesional.setId(1067L);
        profesional.setExternalId(218L);
        profesional.setFirstName("Romina");
        profesional.setLastName("Acosta");
        profesional.setEnabled(true);
        profesional.setCreatedAt(CREADO);
        profesional.setUpdatedAt(ACTUALIZADO);
        profesional.setCategory(categoria);

        ProfessionalDTO dto = ProfessionalDTO.from(profesional);

        assertThat(dto).isEqualTo(
            new ProfessionalDTO(
                1067L,
                218L,
                "Romina",
                "Acosta",
                true,
                CREADO,
                ACTUALIZADO,
                new ProfessionalDTO.Category(1003L, "Ginecologia")
            )
        );
    }

    @Test
    void sinCategoriaNoFalla() {
        Professional profesional = new Professional();
        profesional.setId(1067L);

        assertThat(ProfessionalDTO.from(profesional).category()).isNull();
    }
}

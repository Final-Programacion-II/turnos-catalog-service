package ar.edu.um.turnos.catalog.web.rest;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.um.turnos.catalog.IntegrationTest;
import ar.edu.um.turnos.catalog.domain.Professional;
import ar.edu.um.turnos.catalog.domain.ProfessionalCategory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
@Transactional
class ProfessionalSearchIT {

    private static final String URL = "/api/professionals/search";

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc mockMvc;

    private ProfessionalCategory clinica;
    private ProfessionalCategory pediatria;

    @BeforeEach
    void cargarDatos() {
        clinica = categoria(9001L, "Clinica", true);
        pediatria = categoria(9002L, "Pediatria", false);

        profesional(9101L, "Ana", "Quiroga", true, clinica);
        profesional(9102L, "Bruno", "Quiroga", false, clinica);
        profesional(9103L, "Carla", "Sosa", true, pediatria);
        em.flush();
    }

    @Test
    void buscaPorParteDelNombreSinImportarMayusculas() throws Exception {
        mockMvc
            .perform(get(URL).param("name", "QUIRO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].firstName").value(contains("Ana", "Bruno")));
    }

    @Test
    void buscaPorNombreYApellidoJuntos() throws Exception {
        mockMvc.perform(get(URL).param("name", "carla sosa")).andExpect(jsonPath("$[*].firstName").value(contains("Carla")));
    }

    @Test
    void buscaPorCategoria() throws Exception {
        mockMvc
            .perform(get(URL).param("categoryId", clinica.getId().toString()))
            .andExpect(jsonPath("$[*].firstName").value(contains("Ana", "Bruno")));
    }

    @Test
    void habilitadoTieneEnCuentaLaCategoria() throws Exception {
        // Carla está habilitada, pero su categoría no: no se puede reservar con ella
        mockMvc
            .perform(get(URL).param("categoryId", pediatria.getId().toString()).param("enabled", "true"))
            .andExpect(jsonPath("$").value(empty()));

        mockMvc
            .perform(get(URL).param("categoryId", pediatria.getId().toString()).param("enabled", "false"))
            .andExpect(jsonPath("$[*].firstName").value(contains("Carla")));
    }

    @Test
    void combinaFiltros() throws Exception {
        mockMvc
            .perform(get(URL).param("name", "quiroga").param("enabled", "true"))
            .andExpect(jsonPath("$[*].firstName").value(contains("Ana")));
    }

    @Test
    void nombreVacioNoFiltra() throws Exception {
        mockMvc
            .perform(get(URL).param("categoryId", clinica.getId().toString()).param("name", "  "))
            .andExpect(jsonPath("$[*].firstName").value(contains("Ana", "Bruno")));
    }

    private ProfessionalCategory categoria(Long externalId, String nombre, boolean habilitada) {
        ProfessionalCategory categoria = new ProfessionalCategory().externalId(externalId).name(nombre).enabled(habilitada);
        em.persist(categoria);
        return categoria;
    }

    private void profesional(Long externalId, String nombre, String apellido, boolean habilitado, ProfessionalCategory categoria) {
        em.persist(new Professional().externalId(externalId).firstName(nombre).lastName(apellido).enabled(habilitado).category(categoria));
    }
}

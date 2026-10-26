package ar.edu.um.turnos.catalog.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.um.turnos.catalog.IntegrationTest;
import ar.edu.um.turnos.catalog.domain.Professional;
import ar.edu.um.turnos.catalog.domain.ProfessionalCategory;
import ar.edu.um.turnos.catalog.domain.WeeklySchedule;
import ar.edu.um.turnos.catalog.domain.enumeration.Weekday;
import jakarta.persistence.EntityManager;
import java.time.LocalTime;
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

        Professional ana = profesional(9101L, "Ana", "Quiroga", true, clinica);
        Professional bruno = profesional(9102L, "Bruno", "Quiroga", false, clinica);
        Professional carla = profesional(9103L, "Carla", "Sosa", true, pediatria);

        horario(9201L, ana, Weekday.MONDAY, "09:00", "12:00", true);
        horario(9202L, ana, Weekday.WEDNESDAY, "09:00", "12:00", false);
        horario(9203L, bruno, Weekday.MONDAY, "14:00", "18:00", true);
        horario(9204L, carla, Weekday.TUESDAY, "09:00", "12:00", true);
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

    @Test
    void buscaPorDia() throws Exception {
        mockMvc
            .perform(get(URL).param("categoryId", clinica.getId().toString()).param("day", "MONDAY"))
            .andExpect(jsonPath("$[*].firstName").value(contains("Ana", "Bruno")));
    }

    @Test
    void buscaPorDiaYFranja() throws Exception {
        // Ana atiende de 9 a 12 y Bruno de 14 a 18: a la tarde solo queda Bruno
        mockMvc
            .perform(get(URL).param("categoryId", clinica.getId().toString()).param("day", "MONDAY").param("from", "13:00"))
            .andExpect(jsonPath("$[*].firstName").value(contains("Bruno")));

        mockMvc
            .perform(get(URL).param("categoryId", clinica.getId().toString()).param("day", "MONDAY").param("to", "10:00"))
            .andExpect(jsonPath("$[*].firstName").value(contains("Ana")));
    }

    @Test
    void franjaQueSeSuperponeConElHorario() throws Exception {
        // 11 a 15 toca el final del horario de Ana y el principio del de Bruno
        mockMvc
            .perform(get(URL).param("categoryId", clinica.getId().toString()).param("from", "11:00").param("to", "15:00"))
            .andExpect(jsonPath("$[*].firstName").value(contains("Ana", "Bruno")));
    }

    @Test
    void horarioDeshabilitadoNoCuenta() throws Exception {
        mockMvc
            .perform(get(URL).param("categoryId", clinica.getId().toString()).param("day", "WEDNESDAY"))
            .andExpect(jsonPath("$").value(empty()));
    }

    @Test
    void horarioQueTerminaDeNocheSeGuardaYSeBuscaBien() throws Exception {
        ProfessionalCategory guardia = categoria(9003L, "Guardia", true);
        Professional diego = profesional(9104L, "Diego", "Vera", true, guardia);
        horario(9205L, diego, Weekday.MONDAY, "18:00", "21:00", true);
        em.flush();

        Object guardado = em
            .createNativeQuery("select cast(end_time as varchar) from weekly_schedule where external_id = 9205")
            .getSingleResult();
        assertThat(guardado).isEqualTo("21:00:00");

        mockMvc
            .perform(get(URL).param("categoryId", guardia.getId().toString()).param("day", "MONDAY").param("from", "14:00"))
            .andExpect(jsonPath("$[*].firstName").value(contains("Diego")));
    }

    @Test
    void franjaInvalidaDevuelve400() throws Exception {
        mockMvc.perform(get(URL).param("from", "12:00").param("to", "09:00")).andExpect(status().isBadRequest());
    }

    private ProfessionalCategory categoria(Long externalId, String nombre, boolean habilitada) {
        ProfessionalCategory categoria = new ProfessionalCategory().externalId(externalId).name(nombre).enabled(habilitada);
        em.persist(categoria);
        return categoria;
    }

    private Professional profesional(Long externalId, String nombre, String apellido, boolean habilitado, ProfessionalCategory categoria) {
        Professional profesional = new Professional()
            .externalId(externalId)
            .firstName(nombre)
            .lastName(apellido)
            .enabled(habilitado)
            .category(categoria);
        em.persist(profesional);
        return profesional;
    }

    private void horario(Long externalId, Professional profesional, Weekday dia, String desde, String hasta, boolean habilitado) {
        em.persist(
            new WeeklySchedule()
                .externalId(externalId)
                .professional(profesional)
                .dayOfWeek(dia)
                .startTime(LocalTime.parse(desde))
                .endTime(LocalTime.parse(hasta))
                .slotDurationMinutes(30)
                .enabled(habilitado)
        );
    }
}

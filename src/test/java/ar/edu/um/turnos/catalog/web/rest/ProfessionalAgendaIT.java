package ar.edu.um.turnos.catalog.web.rest;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.um.turnos.catalog.IntegrationTest;
import ar.edu.um.turnos.catalog.repository.CatalogSyncStateRepository;
import ar.edu.um.turnos.catalog.repository.entity.CatalogSyncState;
import ar.edu.um.turnos.catalog.repository.entity.Professional;
import ar.edu.um.turnos.catalog.repository.entity.ProfessionalCategory;
import ar.edu.um.turnos.catalog.repository.entity.WeeklySchedule;
import ar.edu.um.turnos.catalog.repository.entity.enumeration.Weekday;
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
class ProfessionalAgendaIT {

    private static final String URL = "/api/professionals/{id}/agenda";

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CatalogSyncStateRepository catalogSyncStateRepository;

    private ProfessionalCategory clinica;

    @BeforeEach
    void cargarDatos() {
        clinica = categoria(9011L, true);
    }

    @Test
    void devuelveSoloLosHorariosHabilitadosOrdenadosPorDiaYHora() throws Exception {
        Professional ana = profesional(9111L, true, clinica);
        // Se cargan desordenados a propósito
        horario(9211L, ana, Weekday.WEDNESDAY, "09:00", "12:00", true);
        horario(9212L, ana, Weekday.MONDAY, "14:00", "18:00", true);
        horario(9213L, ana, Weekday.MONDAY, "09:00", "12:00", true);
        horario(9214L, ana, Weekday.FRIDAY, "09:00", "12:00", false);
        em.flush();

        mockMvc
            .perform(get(URL, ana.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.externalId").value(9111))
            .andExpect(jsonPath("$.categoryName").value("Clinica"))
            .andExpect(jsonPath("$.bookable").value(true))
            .andExpect(jsonPath("$.schedules", hasSize(3)))
            .andExpect(jsonPath("$.schedules[*].dayOfWeek").value(contains("MONDAY", "MONDAY", "WEDNESDAY")))
            .andExpect(jsonPath("$.schedules[*].startTime").value(contains("09:00:00", "14:00:00", "09:00:00")))
            .andExpect(jsonPath("$.schedules[0].externalId").value(9213))
            .andExpect(jsonPath("$.schedules[0].slotDurationMinutes").value(30));
    }

    @Test
    void profesionalDadoDeBajaNoSePuedeReservar() throws Exception {
        Professional bruno = profesional(9112L, false, clinica);
        em.flush();

        mockMvc.perform(get(URL, bruno.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.bookable").value(false));
    }

    @Test
    void categoriaDadaDeBajaTampocoSePuedeReservar() throws Exception {
        ProfessionalCategory pediatria = categoria(9012L, false);
        Professional carla = profesional(9113L, true, pediatria);
        em.flush();

        mockMvc.perform(get(URL, carla.getId())).andExpect(jsonPath("$.bookable").value(false));
    }

    @Test
    void incluyeLaVersionDelCatalogo() throws Exception {
        Professional ana = profesional(9111L, true, clinica);
        CatalogSyncState estado = catalogSyncStateRepository.findById(CatalogSyncState.SINGLETON_ID).orElseThrow();
        estado.setCatalogVersion(7L);
        em.flush();

        mockMvc.perform(get(URL, ana.getId())).andExpect(jsonPath("$.catalogVersion").value(7));
    }

    @Test
    void profesionalInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get(URL, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    private ProfessionalCategory categoria(Long externalId, boolean habilitada) {
        ProfessionalCategory categoria = new ProfessionalCategory();
        categoria.setExternalId(externalId);
        categoria.setName(habilitada ? "Clinica" : "Pediatria");
        categoria.setEnabled(habilitada);
        em.persist(categoria);
        return categoria;
    }

    private Professional profesional(Long externalId, boolean habilitado, ProfessionalCategory categoria) {
        Professional profesional = new Professional();
        profesional.setExternalId(externalId);
        profesional.setFirstName("Nombre");
        profesional.setLastName("Apellido");
        profesional.setEnabled(habilitado);
        profesional.setCategory(categoria);
        em.persist(profesional);
        return profesional;
    }

    private void horario(Long externalId, Professional profesional, Weekday dia, String desde, String hasta, boolean habilitado) {
        WeeklySchedule horario = new WeeklySchedule();
        horario.setExternalId(externalId);
        horario.setProfessional(profesional);
        horario.setDayOfWeek(dia);
        horario.setStartTime(LocalTime.parse(desde));
        horario.setEndTime(LocalTime.parse(hasta));
        horario.setSlotDurationMinutes(30);
        horario.setEnabled(habilitado);
        em.persist(horario);
    }
}

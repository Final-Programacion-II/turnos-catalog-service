package ar.edu.um.turnos.catalog.web.rest;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ar.edu.um.turnos.catalog.IntegrationTest;
import ar.edu.um.turnos.catalog.repository.WeeklyScheduleRepository;
import ar.edu.um.turnos.catalog.repository.entity.Professional;
import ar.edu.um.turnos.catalog.repository.entity.WeeklySchedule;
import ar.edu.um.turnos.catalog.repository.entity.enumeration.Weekday;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link WeeklyScheduleResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class WeeklyScheduleResourceIT {

    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final Long DEFAULT_EXTERNAL_ID = 1L;

    private static final Weekday DEFAULT_DAY_OF_WEEK = Weekday.MONDAY;

    private static final LocalTime DEFAULT_START_TIME = LocalTime.NOON;

    private static final LocalTime DEFAULT_END_TIME = LocalTime.NOON;

    private static final Integer DEFAULT_SLOT_DURATION_MINUTES = 1;

    private static final Boolean DEFAULT_ENABLED = false;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);

    private static final String ENTITY_API_URL = "/api/weekly-schedules";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private WeeklyScheduleRepository weeklyScheduleRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restWeeklyScheduleMockMvc;

    private WeeklySchedule weeklySchedule;

    private WeeklySchedule insertedWeeklySchedule;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static WeeklySchedule createEntity(EntityManager em) {
        WeeklySchedule weeklySchedule = new WeeklySchedule();
        weeklySchedule.setExternalId(DEFAULT_EXTERNAL_ID);
        weeklySchedule.setDayOfWeek(DEFAULT_DAY_OF_WEEK);
        weeklySchedule.setStartTime(DEFAULT_START_TIME);
        weeklySchedule.setEndTime(DEFAULT_END_TIME);
        weeklySchedule.setSlotDurationMinutes(DEFAULT_SLOT_DURATION_MINUTES);
        weeklySchedule.setEnabled(DEFAULT_ENABLED);
        weeklySchedule.setCreatedAt(DEFAULT_CREATED_AT);
        weeklySchedule.setUpdatedAt(DEFAULT_UPDATED_AT);
        // Add required entity
        Professional professional;
        if (TestUtil.findAll(em, Professional.class).isEmpty()) {
            professional = ProfessionalResourceIT.createEntity(em);
            em.persist(professional);
            em.flush();
        } else {
            professional = TestUtil.findAll(em, Professional.class).get(0);
        }
        weeklySchedule.setProfessional(professional);
        return weeklySchedule;
    }

    @BeforeEach
    void initTest() {
        weeklySchedule = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedWeeklySchedule != null) {
            weeklyScheduleRepository.delete(insertedWeeklySchedule);
            insertedWeeklySchedule = null;
        }
    }

    @Test
    @Transactional
    void getAllWeeklySchedules() throws Exception {
        // Initialize the database
        insertedWeeklySchedule = weeklyScheduleRepository.saveAndFlush(weeklySchedule);

        // Get all the weeklyScheduleList
        restWeeklyScheduleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(weeklySchedule.getId().intValue())))
            .andExpect(jsonPath("$.[*].externalId").value(hasItem(DEFAULT_EXTERNAL_ID.intValue())))
            .andExpect(jsonPath("$.[*].dayOfWeek").value(hasItem(DEFAULT_DAY_OF_WEEK.toString())))
            .andExpect(jsonPath("$.[*].startTime").value(hasItem(DEFAULT_START_TIME.format(LOCAL_DATE_TIME_FORMAT))))
            .andExpect(jsonPath("$.[*].endTime").value(hasItem(DEFAULT_END_TIME.format(LOCAL_DATE_TIME_FORMAT))))
            .andExpect(jsonPath("$.[*].slotDurationMinutes").value(hasItem(DEFAULT_SLOT_DURATION_MINUTES)))
            .andExpect(jsonPath("$.[*].enabled").value(hasItem(DEFAULT_ENABLED)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    @Test
    @Transactional
    void getWeeklySchedule() throws Exception {
        // Initialize the database
        insertedWeeklySchedule = weeklyScheduleRepository.saveAndFlush(weeklySchedule);

        // Get the weeklySchedule
        restWeeklyScheduleMockMvc
            .perform(get(ENTITY_API_URL_ID, weeklySchedule.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(weeklySchedule.getId().intValue()))
            .andExpect(jsonPath("$.externalId").value(DEFAULT_EXTERNAL_ID.intValue()))
            .andExpect(jsonPath("$.dayOfWeek").value(DEFAULT_DAY_OF_WEEK.toString()))
            .andExpect(jsonPath("$.startTime").value(DEFAULT_START_TIME.format(LOCAL_DATE_TIME_FORMAT)))
            .andExpect(jsonPath("$.endTime").value(DEFAULT_END_TIME.format(LOCAL_DATE_TIME_FORMAT)))
            .andExpect(jsonPath("$.slotDurationMinutes").value(DEFAULT_SLOT_DURATION_MINUTES))
            .andExpect(jsonPath("$.enabled").value(DEFAULT_ENABLED))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()))
            // Del profesional viaja solo el id
            .andExpect(jsonPath("$.professional.id").value(weeklySchedule.getProfessional().getId().intValue()));
    }

    @Test
    @Transactional
    void getNonExistingWeeklySchedule() throws Exception {
        // Get the weeklySchedule
        restWeeklyScheduleMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }
}

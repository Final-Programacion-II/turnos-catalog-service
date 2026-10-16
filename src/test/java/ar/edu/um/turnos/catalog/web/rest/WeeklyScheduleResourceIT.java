package ar.edu.um.turnos.catalog.web.rest;

import static ar.edu.um.turnos.catalog.domain.WeeklyScheduleAsserts.*;
import static ar.edu.um.turnos.catalog.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ar.edu.um.turnos.catalog.IntegrationTest;
import ar.edu.um.turnos.catalog.domain.Professional;
import ar.edu.um.turnos.catalog.domain.WeeklySchedule;
import ar.edu.um.turnos.catalog.domain.enumeration.Weekday;
import ar.edu.um.turnos.catalog.repository.WeeklyScheduleRepository;
import ar.edu.um.turnos.catalog.service.dto.WeeklyScheduleDTO;
import ar.edu.um.turnos.catalog.service.mapper.WeeklyScheduleMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
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
    private static final Long UPDATED_EXTERNAL_ID = 2L;

    private static final Weekday DEFAULT_DAY_OF_WEEK = Weekday.MONDAY;
    private static final Weekday UPDATED_DAY_OF_WEEK = Weekday.TUESDAY;

    private static final LocalTime DEFAULT_START_TIME = LocalTime.NOON;
    private static final LocalTime UPDATED_START_TIME = LocalTime.MAX.withNano(0);

    private static final LocalTime DEFAULT_END_TIME = LocalTime.NOON;
    private static final LocalTime UPDATED_END_TIME = LocalTime.MAX.withNano(0);

    private static final Integer DEFAULT_SLOT_DURATION_MINUTES = 1;
    private static final Integer UPDATED_SLOT_DURATION_MINUTES = 2;

    private static final Boolean DEFAULT_ENABLED = false;
    private static final Boolean UPDATED_ENABLED = true;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.ofEpochMilli(1704076426520L);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.ofEpochMilli(1704076426520L);

    private static final String ENTITY_API_URL = "/api/weekly-schedules";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private WeeklyScheduleRepository weeklyScheduleRepository;

    @Autowired
    private WeeklyScheduleMapper weeklyScheduleMapper;

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
        WeeklySchedule weeklySchedule = new WeeklySchedule()
            .externalId(DEFAULT_EXTERNAL_ID)
            .dayOfWeek(DEFAULT_DAY_OF_WEEK)
            .startTime(DEFAULT_START_TIME)
            .endTime(DEFAULT_END_TIME)
            .slotDurationMinutes(DEFAULT_SLOT_DURATION_MINUTES)
            .enabled(DEFAULT_ENABLED)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
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

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static WeeklySchedule createUpdatedEntity(EntityManager em) {
        WeeklySchedule updatedWeeklySchedule = new WeeklySchedule()
            .externalId(UPDATED_EXTERNAL_ID)
            .dayOfWeek(UPDATED_DAY_OF_WEEK)
            .startTime(UPDATED_START_TIME)
            .endTime(UPDATED_END_TIME)
            .slotDurationMinutes(UPDATED_SLOT_DURATION_MINUTES)
            .enabled(UPDATED_ENABLED)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        // Add required entity
        Professional professional;
        if (TestUtil.findAll(em, Professional.class).isEmpty()) {
            professional = ProfessionalResourceIT.createUpdatedEntity(em);
            em.persist(professional);
            em.flush();
        } else {
            professional = TestUtil.findAll(em, Professional.class).get(0);
        }
        updatedWeeklySchedule.setProfessional(professional);
        return updatedWeeklySchedule;
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
    void createWeeklySchedule() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the WeeklySchedule
        WeeklyScheduleDTO weeklyScheduleDTO = weeklyScheduleMapper.toDto(weeklySchedule);
        var returnedWeeklyScheduleDTO = om.readValue(
            restWeeklyScheduleMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(weeklyScheduleDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            WeeklyScheduleDTO.class
        );

        // Validate the WeeklySchedule in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedWeeklySchedule = weeklyScheduleMapper.toEntity(returnedWeeklyScheduleDTO);
        assertWeeklyScheduleUpdatableFieldsEquals(returnedWeeklySchedule, getPersistedWeeklySchedule(returnedWeeklySchedule));

        insertedWeeklySchedule = returnedWeeklySchedule;
    }

    @Test
    @Transactional
    void createWeeklyScheduleWithExistingId() throws Exception {
        // Create the WeeklySchedule with an existing ID
        weeklySchedule.setId(1L);
        WeeklyScheduleDTO weeklyScheduleDTO = weeklyScheduleMapper.toDto(weeklySchedule);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restWeeklyScheduleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(weeklyScheduleDTO)))
            .andExpect(status().isBadRequest());

        // Validate the WeeklySchedule in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkExternalIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        weeklySchedule.setExternalId(null);

        // Create the WeeklySchedule, which fails.
        WeeklyScheduleDTO weeklyScheduleDTO = weeklyScheduleMapper.toDto(weeklySchedule);

        restWeeklyScheduleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(weeklyScheduleDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDayOfWeekIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        weeklySchedule.setDayOfWeek(null);

        // Create the WeeklySchedule, which fails.
        WeeklyScheduleDTO weeklyScheduleDTO = weeklyScheduleMapper.toDto(weeklySchedule);

        restWeeklyScheduleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(weeklyScheduleDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStartTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        weeklySchedule.setStartTime(null);

        // Create the WeeklySchedule, which fails.
        WeeklyScheduleDTO weeklyScheduleDTO = weeklyScheduleMapper.toDto(weeklySchedule);

        restWeeklyScheduleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(weeklyScheduleDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEndTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        weeklySchedule.setEndTime(null);

        // Create the WeeklySchedule, which fails.
        WeeklyScheduleDTO weeklyScheduleDTO = weeklyScheduleMapper.toDto(weeklySchedule);

        restWeeklyScheduleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(weeklyScheduleDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSlotDurationMinutesIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        weeklySchedule.setSlotDurationMinutes(null);

        // Create the WeeklySchedule, which fails.
        WeeklyScheduleDTO weeklyScheduleDTO = weeklyScheduleMapper.toDto(weeklySchedule);

        restWeeklyScheduleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(weeklyScheduleDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEnabledIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        weeklySchedule.setEnabled(null);

        // Create the WeeklySchedule, which fails.
        WeeklyScheduleDTO weeklyScheduleDTO = weeklyScheduleMapper.toDto(weeklySchedule);

        restWeeklyScheduleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(weeklyScheduleDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
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
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingWeeklySchedule() throws Exception {
        // Get the weeklySchedule
        restWeeklyScheduleMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingWeeklySchedule() throws Exception {
        // Initialize the database
        insertedWeeklySchedule = weeklyScheduleRepository.saveAndFlush(weeklySchedule);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the weeklySchedule
        WeeklySchedule updatedWeeklySchedule = weeklyScheduleRepository.findById(weeklySchedule.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedWeeklySchedule are not directly saved in db
        em.detach(updatedWeeklySchedule);
        updatedWeeklySchedule
            .externalId(UPDATED_EXTERNAL_ID)
            .dayOfWeek(UPDATED_DAY_OF_WEEK)
            .startTime(UPDATED_START_TIME)
            .endTime(UPDATED_END_TIME)
            .slotDurationMinutes(UPDATED_SLOT_DURATION_MINUTES)
            .enabled(UPDATED_ENABLED)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        WeeklyScheduleDTO weeklyScheduleDTO = weeklyScheduleMapper.toDto(updatedWeeklySchedule);

        restWeeklyScheduleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, weeklyScheduleDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(weeklyScheduleDTO))
            )
            .andExpect(status().isOk());

        // Validate the WeeklySchedule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedWeeklyScheduleToMatchAllProperties(updatedWeeklySchedule);
    }

    @Test
    @Transactional
    void putNonExistingWeeklySchedule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        weeklySchedule.setId(longCount.incrementAndGet());

        // Create the WeeklySchedule
        WeeklyScheduleDTO weeklyScheduleDTO = weeklyScheduleMapper.toDto(weeklySchedule);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWeeklyScheduleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, weeklyScheduleDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(weeklyScheduleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WeeklySchedule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchWeeklySchedule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        weeklySchedule.setId(longCount.incrementAndGet());

        // Create the WeeklySchedule
        WeeklyScheduleDTO weeklyScheduleDTO = weeklyScheduleMapper.toDto(weeklySchedule);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWeeklyScheduleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(weeklyScheduleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WeeklySchedule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamWeeklySchedule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        weeklySchedule.setId(longCount.incrementAndGet());

        // Create the WeeklySchedule
        WeeklyScheduleDTO weeklyScheduleDTO = weeklyScheduleMapper.toDto(weeklySchedule);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWeeklyScheduleMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(weeklyScheduleDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the WeeklySchedule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateWeeklyScheduleWithPatch() throws Exception {
        // Initialize the database
        insertedWeeklySchedule = weeklyScheduleRepository.saveAndFlush(weeklySchedule);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the weeklySchedule using partial update
        WeeklySchedule partialUpdatedWeeklySchedule = new WeeklySchedule();
        partialUpdatedWeeklySchedule.setId(weeklySchedule.getId());

        partialUpdatedWeeklySchedule.enabled(UPDATED_ENABLED).updatedAt(UPDATED_UPDATED_AT);

        restWeeklyScheduleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWeeklySchedule.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedWeeklySchedule))
            )
            .andExpect(status().isOk());

        // Validate the WeeklySchedule in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertWeeklyScheduleUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedWeeklySchedule, weeklySchedule),
            getPersistedWeeklySchedule(weeklySchedule)
        );
    }

    @Test
    @Transactional
    void fullUpdateWeeklyScheduleWithPatch() throws Exception {
        // Initialize the database
        insertedWeeklySchedule = weeklyScheduleRepository.saveAndFlush(weeklySchedule);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the weeklySchedule using partial update
        WeeklySchedule partialUpdatedWeeklySchedule = new WeeklySchedule();
        partialUpdatedWeeklySchedule.setId(weeklySchedule.getId());

        partialUpdatedWeeklySchedule
            .externalId(UPDATED_EXTERNAL_ID)
            .dayOfWeek(UPDATED_DAY_OF_WEEK)
            .startTime(UPDATED_START_TIME)
            .endTime(UPDATED_END_TIME)
            .slotDurationMinutes(UPDATED_SLOT_DURATION_MINUTES)
            .enabled(UPDATED_ENABLED)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restWeeklyScheduleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWeeklySchedule.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedWeeklySchedule))
            )
            .andExpect(status().isOk());

        // Validate the WeeklySchedule in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertWeeklyScheduleUpdatableFieldsEquals(partialUpdatedWeeklySchedule, getPersistedWeeklySchedule(partialUpdatedWeeklySchedule));
    }

    @Test
    @Transactional
    void patchNonExistingWeeklySchedule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        weeklySchedule.setId(longCount.incrementAndGet());

        // Create the WeeklySchedule
        WeeklyScheduleDTO weeklyScheduleDTO = weeklyScheduleMapper.toDto(weeklySchedule);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWeeklyScheduleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, weeklyScheduleDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(weeklyScheduleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WeeklySchedule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchWeeklySchedule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        weeklySchedule.setId(longCount.incrementAndGet());

        // Create the WeeklySchedule
        WeeklyScheduleDTO weeklyScheduleDTO = weeklyScheduleMapper.toDto(weeklySchedule);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWeeklyScheduleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(weeklyScheduleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WeeklySchedule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamWeeklySchedule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        weeklySchedule.setId(longCount.incrementAndGet());

        // Create the WeeklySchedule
        WeeklyScheduleDTO weeklyScheduleDTO = weeklyScheduleMapper.toDto(weeklySchedule);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWeeklyScheduleMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(weeklyScheduleDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the WeeklySchedule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteWeeklySchedule() throws Exception {
        // Initialize the database
        insertedWeeklySchedule = weeklyScheduleRepository.saveAndFlush(weeklySchedule);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the weeklySchedule
        restWeeklyScheduleMockMvc
            .perform(delete(ENTITY_API_URL_ID, weeklySchedule.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return weeklyScheduleRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected WeeklySchedule getPersistedWeeklySchedule(WeeklySchedule weeklySchedule) {
        return weeklyScheduleRepository.findById(weeklySchedule.getId()).orElseThrow();
    }

    protected void assertPersistedWeeklyScheduleToMatchAllProperties(WeeklySchedule expectedWeeklySchedule) {
        assertWeeklyScheduleAllPropertiesEquals(expectedWeeklySchedule, getPersistedWeeklySchedule(expectedWeeklySchedule));
    }

    protected void assertPersistedWeeklyScheduleToMatchUpdatableProperties(WeeklySchedule expectedWeeklySchedule) {
        assertWeeklyScheduleAllUpdatablePropertiesEquals(expectedWeeklySchedule, getPersistedWeeklySchedule(expectedWeeklySchedule));
    }
}

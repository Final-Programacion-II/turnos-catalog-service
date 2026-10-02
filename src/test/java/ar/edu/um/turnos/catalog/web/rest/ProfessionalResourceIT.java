package ar.edu.um.turnos.catalog.web.rest;

import static ar.edu.um.turnos.catalog.domain.ProfessionalAsserts.*;
import static ar.edu.um.turnos.catalog.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ar.edu.um.turnos.catalog.IntegrationTest;
import ar.edu.um.turnos.catalog.domain.Professional;
import ar.edu.um.turnos.catalog.domain.ProfessionalCategory;
import ar.edu.um.turnos.catalog.repository.ProfessionalRepository;
import ar.edu.um.turnos.catalog.service.ProfessionalService;
import ar.edu.um.turnos.catalog.service.dto.ProfessionalDTO;
import ar.edu.um.turnos.catalog.service.mapper.ProfessionalMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ProfessionalResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ProfessionalResourceIT {

    private static final Long DEFAULT_EXTERNAL_ID = 1L;
    private static final Long UPDATED_EXTERNAL_ID = 2L;

    private static final String DEFAULT_FIRST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FIRST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LAST_NAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ENABLED = false;
    private static final Boolean UPDATED_ENABLED = true;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.ofEpochMilli(1704076426520L);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.ofEpochMilli(1704076426520L);

    private static final String ENTITY_API_URL = "/api/professionals";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ProfessionalRepository professionalRepository;

    @Mock
    private ProfessionalRepository professionalRepositoryMock;

    @Autowired
    private ProfessionalMapper professionalMapper;

    @Mock
    private ProfessionalService professionalServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProfessionalMockMvc;

    private Professional professional;

    private Professional insertedProfessional;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Professional createEntity(EntityManager em) {
        Professional professional = new Professional()
            .externalId(DEFAULT_EXTERNAL_ID)
            .firstName(DEFAULT_FIRST_NAME)
            .lastName(DEFAULT_LAST_NAME)
            .enabled(DEFAULT_ENABLED)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
        // Add required entity
        ProfessionalCategory professionalCategory;
        if (TestUtil.findAll(em, ProfessionalCategory.class).isEmpty()) {
            professionalCategory = ProfessionalCategoryResourceIT.createEntity();
            em.persist(professionalCategory);
            em.flush();
        } else {
            professionalCategory = TestUtil.findAll(em, ProfessionalCategory.class).get(0);
        }
        professional.setCategory(professionalCategory);
        return professional;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Professional createUpdatedEntity(EntityManager em) {
        Professional updatedProfessional = new Professional()
            .externalId(UPDATED_EXTERNAL_ID)
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .enabled(UPDATED_ENABLED)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        // Add required entity
        ProfessionalCategory professionalCategory;
        if (TestUtil.findAll(em, ProfessionalCategory.class).isEmpty()) {
            professionalCategory = ProfessionalCategoryResourceIT.createUpdatedEntity();
            em.persist(professionalCategory);
            em.flush();
        } else {
            professionalCategory = TestUtil.findAll(em, ProfessionalCategory.class).get(0);
        }
        updatedProfessional.setCategory(professionalCategory);
        return updatedProfessional;
    }

    @BeforeEach
    void initTest() {
        professional = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedProfessional != null) {
            professionalRepository.delete(insertedProfessional);
            insertedProfessional = null;
        }
    }

    @Test
    @Transactional
    void createProfessional() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Professional
        ProfessionalDTO professionalDTO = professionalMapper.toDto(professional);
        var returnedProfessionalDTO = om.readValue(
            restProfessionalMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(professionalDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ProfessionalDTO.class
        );

        // Validate the Professional in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedProfessional = professionalMapper.toEntity(returnedProfessionalDTO);
        assertProfessionalUpdatableFieldsEquals(returnedProfessional, getPersistedProfessional(returnedProfessional));

        insertedProfessional = returnedProfessional;
    }

    @Test
    @Transactional
    void createProfessionalWithExistingId() throws Exception {
        // Create the Professional with an existing ID
        professional.setId(1L);
        ProfessionalDTO professionalDTO = professionalMapper.toDto(professional);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restProfessionalMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(professionalDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Professional in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkExternalIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        professional.setExternalId(null);

        // Create the Professional, which fails.
        ProfessionalDTO professionalDTO = professionalMapper.toDto(professional);

        restProfessionalMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(professionalDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkFirstNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        professional.setFirstName(null);

        // Create the Professional, which fails.
        ProfessionalDTO professionalDTO = professionalMapper.toDto(professional);

        restProfessionalMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(professionalDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLastNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        professional.setLastName(null);

        // Create the Professional, which fails.
        ProfessionalDTO professionalDTO = professionalMapper.toDto(professional);

        restProfessionalMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(professionalDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEnabledIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        professional.setEnabled(null);

        // Create the Professional, which fails.
        ProfessionalDTO professionalDTO = professionalMapper.toDto(professional);

        restProfessionalMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(professionalDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllProfessionals() throws Exception {
        // Initialize the database
        insertedProfessional = professionalRepository.saveAndFlush(professional);

        // Get all the professionalList
        restProfessionalMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(professional.getId().intValue())))
            .andExpect(jsonPath("$.[*].externalId").value(hasItem(DEFAULT_EXTERNAL_ID.intValue())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))
            .andExpect(jsonPath("$.[*].enabled").value(hasItem(DEFAULT_ENABLED)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllProfessionalsWithEagerRelationshipsIsEnabled() throws Exception {
        when(professionalServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restProfessionalMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(professionalServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllProfessionalsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(professionalServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restProfessionalMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(professionalRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getProfessional() throws Exception {
        // Initialize the database
        insertedProfessional = professionalRepository.saveAndFlush(professional);

        // Get the professional
        restProfessionalMockMvc
            .perform(get(ENTITY_API_URL_ID, professional.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(professional.getId().intValue()))
            .andExpect(jsonPath("$.externalId").value(DEFAULT_EXTERNAL_ID.intValue()))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRST_NAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LAST_NAME))
            .andExpect(jsonPath("$.enabled").value(DEFAULT_ENABLED))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingProfessional() throws Exception {
        // Get the professional
        restProfessionalMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingProfessional() throws Exception {
        // Initialize the database
        insertedProfessional = professionalRepository.saveAndFlush(professional);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the professional
        Professional updatedProfessional = professionalRepository.findById(professional.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedProfessional are not directly saved in db
        em.detach(updatedProfessional);
        updatedProfessional
            .externalId(UPDATED_EXTERNAL_ID)
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .enabled(UPDATED_ENABLED)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        ProfessionalDTO professionalDTO = professionalMapper.toDto(updatedProfessional);

        restProfessionalMockMvc
            .perform(
                put(ENTITY_API_URL_ID, professionalDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(professionalDTO))
            )
            .andExpect(status().isOk());

        // Validate the Professional in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedProfessionalToMatchAllProperties(updatedProfessional);
    }

    @Test
    @Transactional
    void putNonExistingProfessional() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        professional.setId(longCount.incrementAndGet());

        // Create the Professional
        ProfessionalDTO professionalDTO = professionalMapper.toDto(professional);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProfessionalMockMvc
            .perform(
                put(ENTITY_API_URL_ID, professionalDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(professionalDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Professional in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchProfessional() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        professional.setId(longCount.incrementAndGet());

        // Create the Professional
        ProfessionalDTO professionalDTO = professionalMapper.toDto(professional);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfessionalMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(professionalDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Professional in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamProfessional() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        professional.setId(longCount.incrementAndGet());

        // Create the Professional
        ProfessionalDTO professionalDTO = professionalMapper.toDto(professional);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfessionalMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(professionalDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Professional in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateProfessionalWithPatch() throws Exception {
        // Initialize the database
        insertedProfessional = professionalRepository.saveAndFlush(professional);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the professional using partial update
        Professional partialUpdatedProfessional = new Professional();
        partialUpdatedProfessional.setId(professional.getId());

        partialUpdatedProfessional.firstName(UPDATED_FIRST_NAME).enabled(UPDATED_ENABLED).createdAt(UPDATED_CREATED_AT);

        restProfessionalMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProfessional.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProfessional))
            )
            .andExpect(status().isOk());

        // Validate the Professional in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProfessionalUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedProfessional, professional),
            getPersistedProfessional(professional)
        );
    }

    @Test
    @Transactional
    void fullUpdateProfessionalWithPatch() throws Exception {
        // Initialize the database
        insertedProfessional = professionalRepository.saveAndFlush(professional);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the professional using partial update
        Professional partialUpdatedProfessional = new Professional();
        partialUpdatedProfessional.setId(professional.getId());

        partialUpdatedProfessional
            .externalId(UPDATED_EXTERNAL_ID)
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .enabled(UPDATED_ENABLED)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restProfessionalMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProfessional.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProfessional))
            )
            .andExpect(status().isOk());

        // Validate the Professional in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProfessionalUpdatableFieldsEquals(partialUpdatedProfessional, getPersistedProfessional(partialUpdatedProfessional));
    }

    @Test
    @Transactional
    void patchNonExistingProfessional() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        professional.setId(longCount.incrementAndGet());

        // Create the Professional
        ProfessionalDTO professionalDTO = professionalMapper.toDto(professional);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProfessionalMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, professionalDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(professionalDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Professional in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchProfessional() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        professional.setId(longCount.incrementAndGet());

        // Create the Professional
        ProfessionalDTO professionalDTO = professionalMapper.toDto(professional);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfessionalMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(professionalDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Professional in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamProfessional() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        professional.setId(longCount.incrementAndGet());

        // Create the Professional
        ProfessionalDTO professionalDTO = professionalMapper.toDto(professional);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfessionalMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(professionalDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Professional in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteProfessional() throws Exception {
        // Initialize the database
        insertedProfessional = professionalRepository.saveAndFlush(professional);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the professional
        restProfessionalMockMvc
            .perform(delete(ENTITY_API_URL_ID, professional.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return professionalRepository.count();
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

    protected Professional getPersistedProfessional(Professional professional) {
        return professionalRepository.findById(professional.getId()).orElseThrow();
    }

    protected void assertPersistedProfessionalToMatchAllProperties(Professional expectedProfessional) {
        assertProfessionalAllPropertiesEquals(expectedProfessional, getPersistedProfessional(expectedProfessional));
    }

    protected void assertPersistedProfessionalToMatchUpdatableProperties(Professional expectedProfessional) {
        assertProfessionalAllUpdatablePropertiesEquals(expectedProfessional, getPersistedProfessional(expectedProfessional));
    }
}

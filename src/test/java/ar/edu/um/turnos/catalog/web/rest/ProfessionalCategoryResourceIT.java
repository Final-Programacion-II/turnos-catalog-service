package ar.edu.um.turnos.catalog.web.rest;

import static ar.edu.um.turnos.catalog.domain.ProfessionalCategoryAsserts.*;
import static ar.edu.um.turnos.catalog.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ar.edu.um.turnos.catalog.IntegrationTest;
import ar.edu.um.turnos.catalog.domain.ProfessionalCategory;
import ar.edu.um.turnos.catalog.repository.ProfessionalCategoryRepository;
import ar.edu.um.turnos.catalog.service.dto.ProfessionalCategoryDTO;
import ar.edu.um.turnos.catalog.service.mapper.ProfessionalCategoryMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
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
 * Integration tests for the {@link ProfessionalCategoryResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ProfessionalCategoryResourceIT {

    private static final Long DEFAULT_EXTERNAL_ID = 1L;
    private static final Long UPDATED_EXTERNAL_ID = 2L;

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ENABLED = false;
    private static final Boolean UPDATED_ENABLED = true;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.ofEpochMilli(1704076426520L);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.ofEpochMilli(1704076426520L);

    private static final String ENTITY_API_URL = "/api/professional-categories";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ProfessionalCategoryRepository professionalCategoryRepository;

    @Autowired
    private ProfessionalCategoryMapper professionalCategoryMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProfessionalCategoryMockMvc;

    private ProfessionalCategory professionalCategory;

    private ProfessionalCategory insertedProfessionalCategory;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProfessionalCategory createEntity() {
        return new ProfessionalCategory()
            .externalId(DEFAULT_EXTERNAL_ID)
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION)
            .enabled(DEFAULT_ENABLED)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProfessionalCategory createUpdatedEntity() {
        return new ProfessionalCategory()
            .externalId(UPDATED_EXTERNAL_ID)
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .enabled(UPDATED_ENABLED)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
    }

    @BeforeEach
    void initTest() {
        professionalCategory = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedProfessionalCategory != null) {
            professionalCategoryRepository.delete(insertedProfessionalCategory);
            insertedProfessionalCategory = null;
        }
    }

    @Test
    @Transactional
    void createProfessionalCategory() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ProfessionalCategory
        ProfessionalCategoryDTO professionalCategoryDTO = professionalCategoryMapper.toDto(professionalCategory);
        var returnedProfessionalCategoryDTO = om.readValue(
            restProfessionalCategoryMockMvc
                .perform(
                    post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(professionalCategoryDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ProfessionalCategoryDTO.class
        );

        // Validate the ProfessionalCategory in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedProfessionalCategory = professionalCategoryMapper.toEntity(returnedProfessionalCategoryDTO);
        assertProfessionalCategoryUpdatableFieldsEquals(
            returnedProfessionalCategory,
            getPersistedProfessionalCategory(returnedProfessionalCategory)
        );

        insertedProfessionalCategory = returnedProfessionalCategory;
    }

    @Test
    @Transactional
    void createProfessionalCategoryWithExistingId() throws Exception {
        // Create the ProfessionalCategory with an existing ID
        professionalCategory.setId(1L);
        ProfessionalCategoryDTO professionalCategoryDTO = professionalCategoryMapper.toDto(professionalCategory);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restProfessionalCategoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(professionalCategoryDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ProfessionalCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkExternalIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        professionalCategory.setExternalId(null);

        // Create the ProfessionalCategory, which fails.
        ProfessionalCategoryDTO professionalCategoryDTO = professionalCategoryMapper.toDto(professionalCategory);

        restProfessionalCategoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(professionalCategoryDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        professionalCategory.setName(null);

        // Create the ProfessionalCategory, which fails.
        ProfessionalCategoryDTO professionalCategoryDTO = professionalCategoryMapper.toDto(professionalCategory);

        restProfessionalCategoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(professionalCategoryDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEnabledIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        professionalCategory.setEnabled(null);

        // Create the ProfessionalCategory, which fails.
        ProfessionalCategoryDTO professionalCategoryDTO = professionalCategoryMapper.toDto(professionalCategory);

        restProfessionalCategoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(professionalCategoryDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllProfessionalCategories() throws Exception {
        // Initialize the database
        insertedProfessionalCategory = professionalCategoryRepository.saveAndFlush(professionalCategory);

        // Get all the professionalCategoryList
        restProfessionalCategoryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(professionalCategory.getId().intValue())))
            .andExpect(jsonPath("$.[*].externalId").value(hasItem(DEFAULT_EXTERNAL_ID.intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].enabled").value(hasItem(DEFAULT_ENABLED)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    @Test
    @Transactional
    void getProfessionalCategory() throws Exception {
        // Initialize the database
        insertedProfessionalCategory = professionalCategoryRepository.saveAndFlush(professionalCategory);

        // Get the professionalCategory
        restProfessionalCategoryMockMvc
            .perform(get(ENTITY_API_URL_ID, professionalCategory.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(professionalCategory.getId().intValue()))
            .andExpect(jsonPath("$.externalId").value(DEFAULT_EXTERNAL_ID.intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.enabled").value(DEFAULT_ENABLED))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingProfessionalCategory() throws Exception {
        // Get the professionalCategory
        restProfessionalCategoryMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingProfessionalCategory() throws Exception {
        // Initialize the database
        insertedProfessionalCategory = professionalCategoryRepository.saveAndFlush(professionalCategory);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the professionalCategory
        ProfessionalCategory updatedProfessionalCategory = professionalCategoryRepository
            .findById(professionalCategory.getId())
            .orElseThrow();
        // Disconnect from session so that the updates on updatedProfessionalCategory are not directly saved in db
        em.detach(updatedProfessionalCategory);
        updatedProfessionalCategory
            .externalId(UPDATED_EXTERNAL_ID)
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .enabled(UPDATED_ENABLED)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        ProfessionalCategoryDTO professionalCategoryDTO = professionalCategoryMapper.toDto(updatedProfessionalCategory);

        restProfessionalCategoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, professionalCategoryDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(professionalCategoryDTO))
            )
            .andExpect(status().isOk());

        // Validate the ProfessionalCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedProfessionalCategoryToMatchAllProperties(updatedProfessionalCategory);
    }

    @Test
    @Transactional
    void putNonExistingProfessionalCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        professionalCategory.setId(longCount.incrementAndGet());

        // Create the ProfessionalCategory
        ProfessionalCategoryDTO professionalCategoryDTO = professionalCategoryMapper.toDto(professionalCategory);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProfessionalCategoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, professionalCategoryDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(professionalCategoryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProfessionalCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchProfessionalCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        professionalCategory.setId(longCount.incrementAndGet());

        // Create the ProfessionalCategory
        ProfessionalCategoryDTO professionalCategoryDTO = professionalCategoryMapper.toDto(professionalCategory);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfessionalCategoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(professionalCategoryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProfessionalCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamProfessionalCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        professionalCategory.setId(longCount.incrementAndGet());

        // Create the ProfessionalCategory
        ProfessionalCategoryDTO professionalCategoryDTO = professionalCategoryMapper.toDto(professionalCategory);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfessionalCategoryMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(professionalCategoryDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ProfessionalCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateProfessionalCategoryWithPatch() throws Exception {
        // Initialize the database
        insertedProfessionalCategory = professionalCategoryRepository.saveAndFlush(professionalCategory);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the professionalCategory using partial update
        ProfessionalCategory partialUpdatedProfessionalCategory = new ProfessionalCategory();
        partialUpdatedProfessionalCategory.setId(professionalCategory.getId());

        partialUpdatedProfessionalCategory.name(UPDATED_NAME).enabled(UPDATED_ENABLED);

        restProfessionalCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProfessionalCategory.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProfessionalCategory))
            )
            .andExpect(status().isOk());

        // Validate the ProfessionalCategory in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProfessionalCategoryUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedProfessionalCategory, professionalCategory),
            getPersistedProfessionalCategory(professionalCategory)
        );
    }

    @Test
    @Transactional
    void fullUpdateProfessionalCategoryWithPatch() throws Exception {
        // Initialize the database
        insertedProfessionalCategory = professionalCategoryRepository.saveAndFlush(professionalCategory);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the professionalCategory using partial update
        ProfessionalCategory partialUpdatedProfessionalCategory = new ProfessionalCategory();
        partialUpdatedProfessionalCategory.setId(professionalCategory.getId());

        partialUpdatedProfessionalCategory
            .externalId(UPDATED_EXTERNAL_ID)
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .enabled(UPDATED_ENABLED)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restProfessionalCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProfessionalCategory.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProfessionalCategory))
            )
            .andExpect(status().isOk());

        // Validate the ProfessionalCategory in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProfessionalCategoryUpdatableFieldsEquals(
            partialUpdatedProfessionalCategory,
            getPersistedProfessionalCategory(partialUpdatedProfessionalCategory)
        );
    }

    @Test
    @Transactional
    void patchNonExistingProfessionalCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        professionalCategory.setId(longCount.incrementAndGet());

        // Create the ProfessionalCategory
        ProfessionalCategoryDTO professionalCategoryDTO = professionalCategoryMapper.toDto(professionalCategory);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProfessionalCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, professionalCategoryDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(professionalCategoryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProfessionalCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchProfessionalCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        professionalCategory.setId(longCount.incrementAndGet());

        // Create the ProfessionalCategory
        ProfessionalCategoryDTO professionalCategoryDTO = professionalCategoryMapper.toDto(professionalCategory);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfessionalCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(professionalCategoryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProfessionalCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamProfessionalCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        professionalCategory.setId(longCount.incrementAndGet());

        // Create the ProfessionalCategory
        ProfessionalCategoryDTO professionalCategoryDTO = professionalCategoryMapper.toDto(professionalCategory);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfessionalCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(professionalCategoryDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ProfessionalCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteProfessionalCategory() throws Exception {
        // Initialize the database
        insertedProfessionalCategory = professionalCategoryRepository.saveAndFlush(professionalCategory);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the professionalCategory
        restProfessionalCategoryMockMvc
            .perform(delete(ENTITY_API_URL_ID, professionalCategory.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return professionalCategoryRepository.count();
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

    protected ProfessionalCategory getPersistedProfessionalCategory(ProfessionalCategory professionalCategory) {
        return professionalCategoryRepository.findById(professionalCategory.getId()).orElseThrow();
    }

    protected void assertPersistedProfessionalCategoryToMatchAllProperties(ProfessionalCategory expectedProfessionalCategory) {
        assertProfessionalCategoryAllPropertiesEquals(
            expectedProfessionalCategory,
            getPersistedProfessionalCategory(expectedProfessionalCategory)
        );
    }

    protected void assertPersistedProfessionalCategoryToMatchUpdatableProperties(ProfessionalCategory expectedProfessionalCategory) {
        assertProfessionalCategoryAllUpdatablePropertiesEquals(
            expectedProfessionalCategory,
            getPersistedProfessionalCategory(expectedProfessionalCategory)
        );
    }
}

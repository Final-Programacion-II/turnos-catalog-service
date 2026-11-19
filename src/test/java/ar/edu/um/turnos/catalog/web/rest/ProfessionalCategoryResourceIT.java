package ar.edu.um.turnos.catalog.web.rest;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ar.edu.um.turnos.catalog.IntegrationTest;
import ar.edu.um.turnos.catalog.repository.ProfessionalCategoryRepository;
import ar.edu.um.turnos.catalog.repository.entity.ProfessionalCategory;
import jakarta.persistence.EntityManager;
import java.time.Instant;
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

    private static final String DEFAULT_NAME = "AAAAAAAAAA";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";

    private static final Boolean DEFAULT_ENABLED = false;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);

    private static final String ENTITY_API_URL = "/api/professional-categories";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ProfessionalCategoryRepository professionalCategoryRepository;

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
        ProfessionalCategory entity = new ProfessionalCategory();
        entity.setExternalId(DEFAULT_EXTERNAL_ID);
        entity.setName(DEFAULT_NAME);
        entity.setDescription(DEFAULT_DESCRIPTION);
        entity.setEnabled(DEFAULT_ENABLED);
        entity.setCreatedAt(DEFAULT_CREATED_AT);
        entity.setUpdatedAt(DEFAULT_UPDATED_AT);
        return entity;
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
}

package ar.edu.um.turnos.catalog.web.rest;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ar.edu.um.turnos.catalog.IntegrationTest;
import ar.edu.um.turnos.catalog.repository.ProfessionalRepository;
import ar.edu.um.turnos.catalog.repository.entity.Professional;
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
 * Integration tests for the {@link ProfessionalResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ProfessionalResourceIT {

    private static final Long DEFAULT_EXTERNAL_ID = 1L;

    private static final String DEFAULT_FIRST_NAME = "AAAAAAAAAA";

    private static final String DEFAULT_LAST_NAME = "AAAAAAAAAA";

    private static final Boolean DEFAULT_ENABLED = false;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);

    private static final String ENTITY_API_URL = "/api/professionals";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ProfessionalRepository professionalRepository;

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
        Professional professional = new Professional();
        professional.setExternalId(DEFAULT_EXTERNAL_ID);
        professional.setFirstName(DEFAULT_FIRST_NAME);
        professional.setLastName(DEFAULT_LAST_NAME);
        professional.setEnabled(DEFAULT_ENABLED);
        professional.setCreatedAt(DEFAULT_CREATED_AT);
        professional.setUpdatedAt(DEFAULT_UPDATED_AT);
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
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()))
            // De la categoría viajan solo el id y el nombre
            .andExpect(jsonPath("$.category.id").value(professional.getCategory().getId().intValue()))
            .andExpect(jsonPath("$.category.name").value(professional.getCategory().getName()))
            .andExpect(jsonPath("$.category.description").doesNotExist());
    }

    @Test
    @Transactional
    void getNonExistingProfessional() throws Exception {
        // Get the professional
        restProfessionalMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }
}

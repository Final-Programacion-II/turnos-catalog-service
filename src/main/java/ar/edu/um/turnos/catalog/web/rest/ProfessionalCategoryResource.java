package ar.edu.um.turnos.catalog.web.rest;

import ar.edu.um.turnos.catalog.repository.ProfessionalCategoryRepository;
import ar.edu.um.turnos.catalog.service.ProfessionalCategoryService;
import ar.edu.um.turnos.catalog.service.dto.ProfessionalCategoryDTO;
import ar.edu.um.turnos.catalog.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link ar.edu.um.turnos.catalog.domain.ProfessionalCategory}.
 */
@RestController
@RequestMapping("/api/professional-categories")
public class ProfessionalCategoryResource {

    private static final Logger LOG = LoggerFactory.getLogger(ProfessionalCategoryResource.class);

    private static final String ENTITY_NAME = "professionalCategory";

    @Value("${jhipster.clientApp.name:catalog}")
    private String applicationName;

    private final ProfessionalCategoryService professionalCategoryService;

    private final ProfessionalCategoryRepository professionalCategoryRepository;

    public ProfessionalCategoryResource(
        ProfessionalCategoryService professionalCategoryService,
        ProfessionalCategoryRepository professionalCategoryRepository
    ) {
        this.professionalCategoryService = professionalCategoryService;
        this.professionalCategoryRepository = professionalCategoryRepository;
    }

    /**
     * {@code POST  /professional-categories} : Create a new professionalCategory.
     *
     * @param professionalCategoryDTO the professionalCategoryDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new professionalCategoryDTO, or with status {@code 400 (Bad Request)} if the professionalCategory has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ProfessionalCategoryDTO> createProfessionalCategory(
        @Valid @RequestBody ProfessionalCategoryDTO professionalCategoryDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to save ProfessionalCategory : {}", professionalCategoryDTO);
        if (professionalCategoryDTO.getId() != null) {
            throw new BadRequestAlertException("A new professionalCategory cannot already have an ID", ENTITY_NAME, "idexists");
        }
        professionalCategoryDTO = professionalCategoryService.save(professionalCategoryDTO);
        return ResponseEntity.created(new URI("/api/professional-categories/" + professionalCategoryDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, professionalCategoryDTO.getId().toString()))
            .body(professionalCategoryDTO);
    }

    /**
     * {@code PUT  /professional-categories/:id} : Updates an existing professionalCategory.
     *
     * @param id the id of the professionalCategoryDTO to save.
     * @param professionalCategoryDTO the professionalCategoryDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated professionalCategoryDTO,
     * or with status {@code 400 (Bad Request)} if the professionalCategoryDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the professionalCategoryDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProfessionalCategoryDTO> updateProfessionalCategory(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ProfessionalCategoryDTO professionalCategoryDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update ProfessionalCategory : {}, {}", id, professionalCategoryDTO);
        if (professionalCategoryDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, professionalCategoryDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!professionalCategoryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        professionalCategoryDTO = professionalCategoryService.update(professionalCategoryDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, professionalCategoryDTO.getId().toString()))
            .body(professionalCategoryDTO);
    }

    /**
     * {@code PATCH  /professional-categories/:id} : Partial updates given fields of an existing professionalCategory, field will ignore if it is null
     *
     * @param id the id of the professionalCategoryDTO to save.
     * @param professionalCategoryDTO the professionalCategoryDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated professionalCategoryDTO,
     * or with status {@code 400 (Bad Request)} if the professionalCategoryDTO is not valid,
     * or with status {@code 404 (Not Found)} if the professionalCategoryDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the professionalCategoryDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ProfessionalCategoryDTO> partialUpdateProfessionalCategory(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ProfessionalCategoryDTO professionalCategoryDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ProfessionalCategory partially : {}, {}", id, professionalCategoryDTO);
        if (professionalCategoryDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, professionalCategoryDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!professionalCategoryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ProfessionalCategoryDTO> result = professionalCategoryService.partialUpdate(professionalCategoryDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, professionalCategoryDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /professional-categories} : get all the Professional Categories.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Professional Categories in body.
     */
    @GetMapping("")
    public List<ProfessionalCategoryDTO> getAllProfessionalCategories() {
        LOG.debug("REST request to get all ProfessionalCategories");
        return professionalCategoryService.findAll();
    }

    /**
     * {@code GET  /professional-categories/:id} : get the "id" professionalCategory.
     *
     * @param id the id of the professionalCategoryDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the professionalCategoryDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalCategoryDTO> getProfessionalCategory(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ProfessionalCategory : {}", id);
        Optional<ProfessionalCategoryDTO> professionalCategoryDTO = professionalCategoryService.findOne(id);
        return ResponseUtil.wrapOrNotFound(professionalCategoryDTO);
    }

    /**
     * {@code DELETE  /professional-categories/:id} : delete the "id" professionalCategory.
     *
     * @param id the id of the professionalCategoryDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfessionalCategory(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete ProfessionalCategory : {}", id);
        professionalCategoryService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}

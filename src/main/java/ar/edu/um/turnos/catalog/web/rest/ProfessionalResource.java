package ar.edu.um.turnos.catalog.web.rest;

import ar.edu.um.turnos.catalog.repository.ProfessionalRepository;
import ar.edu.um.turnos.catalog.service.ProfessionalService;
import ar.edu.um.turnos.catalog.service.dto.ProfessionalDTO;
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
 * REST controller for managing {@link ar.edu.um.turnos.catalog.domain.Professional}.
 */
@RestController
@RequestMapping("/api/professionals")
public class ProfessionalResource {

    private static final Logger LOG = LoggerFactory.getLogger(ProfessionalResource.class);

    private static final String ENTITY_NAME = "professional";

    @Value("${jhipster.clientApp.name:catalog}")
    private String applicationName;

    private final ProfessionalService professionalService;

    private final ProfessionalRepository professionalRepository;

    public ProfessionalResource(ProfessionalService professionalService, ProfessionalRepository professionalRepository) {
        this.professionalService = professionalService;
        this.professionalRepository = professionalRepository;
    }

    /**
     * {@code POST  /professionals} : Create a new professional.
     *
     * @param professionalDTO the professionalDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new professionalDTO, or with status {@code 400 (Bad Request)} if the professional has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ProfessionalDTO> createProfessional(@Valid @RequestBody ProfessionalDTO professionalDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save Professional : {}", professionalDTO);
        if (professionalDTO.getId() != null) {
            throw new BadRequestAlertException("A new professional cannot already have an ID", ENTITY_NAME, "idexists");
        }
        professionalDTO = professionalService.save(professionalDTO);
        return ResponseEntity.created(new URI("/api/professionals/" + professionalDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, professionalDTO.getId().toString()))
            .body(professionalDTO);
    }

    /**
     * {@code PUT  /professionals/:id} : Updates an existing professional.
     *
     * @param id the id of the professionalDTO to save.
     * @param professionalDTO the professionalDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated professionalDTO,
     * or with status {@code 400 (Bad Request)} if the professionalDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the professionalDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProfessionalDTO> updateProfessional(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ProfessionalDTO professionalDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Professional : {}, {}", id, professionalDTO);
        if (professionalDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, professionalDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!professionalRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        professionalDTO = professionalService.update(professionalDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, professionalDTO.getId().toString()))
            .body(professionalDTO);
    }

    /**
     * {@code PATCH  /professionals/:id} : Partial updates given fields of an existing professional, field will ignore if it is null
     *
     * @param id the id of the professionalDTO to save.
     * @param professionalDTO the professionalDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated professionalDTO,
     * or with status {@code 400 (Bad Request)} if the professionalDTO is not valid,
     * or with status {@code 404 (Not Found)} if the professionalDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the professionalDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ProfessionalDTO> partialUpdateProfessional(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ProfessionalDTO professionalDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Professional partially : {}, {}", id, professionalDTO);
        if (professionalDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, professionalDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!professionalRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ProfessionalDTO> result = professionalService.partialUpdate(professionalDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, professionalDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /professionals} : get all the Professionals.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Professionals in body.
     */
    @GetMapping("")
    public List<ProfessionalDTO> getAllProfessionals(
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get all Professionals");
        return professionalService.findAll();
    }

    /**
     * {@code GET  /professionals/:id} : get the "id" professional.
     *
     * @param id the id of the professionalDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the professionalDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalDTO> getProfessional(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Professional : {}", id);
        Optional<ProfessionalDTO> professionalDTO = professionalService.findOne(id);
        return ResponseUtil.wrapOrNotFound(professionalDTO);
    }

    /**
     * {@code DELETE  /professionals/:id} : delete the "id" professional.
     *
     * @param id the id of the professionalDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfessional(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Professional : {}", id);
        professionalService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}

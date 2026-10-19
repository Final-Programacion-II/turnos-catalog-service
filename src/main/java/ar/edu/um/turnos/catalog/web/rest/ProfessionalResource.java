package ar.edu.um.turnos.catalog.web.rest;

import ar.edu.um.turnos.catalog.service.ProfessionalService;
import ar.edu.um.turnos.catalog.service.dto.ProfessionalDTO;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link ar.edu.um.turnos.catalog.domain.Professional}.
 */
@RestController
@RequestMapping("/api/professionals")
public class ProfessionalResource {

    private static final Logger LOG = LoggerFactory.getLogger(ProfessionalResource.class);

    private final ProfessionalService professionalService;

    public ProfessionalResource(ProfessionalService professionalService) {
        this.professionalService = professionalService;
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
}

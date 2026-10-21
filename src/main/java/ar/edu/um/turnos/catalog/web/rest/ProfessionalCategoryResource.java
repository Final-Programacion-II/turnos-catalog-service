package ar.edu.um.turnos.catalog.web.rest;

import ar.edu.um.turnos.catalog.service.ProfessionalCategoryService;
import ar.edu.um.turnos.catalog.service.dto.ProfessionalCategoryDTO;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link ar.edu.um.turnos.catalog.domain.ProfessionalCategory}.
 */
@RestController
@RequestMapping("/api/professional-categories")
public class ProfessionalCategoryResource {

    private static final Logger LOG = LoggerFactory.getLogger(ProfessionalCategoryResource.class);

    private final ProfessionalCategoryService professionalCategoryService;

    public ProfessionalCategoryResource(ProfessionalCategoryService professionalCategoryService) {
        this.professionalCategoryService = professionalCategoryService;
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
}

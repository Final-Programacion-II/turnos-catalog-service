package ar.edu.um.turnos.catalog.web.rest;

import ar.edu.um.turnos.catalog.service.ProfessionalAgendaService;
import ar.edu.um.turnos.catalog.service.dto.ProfessionalAgendaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.jhipster.web.util.ResponseUtil;

/**
 * Agenda vigente de un profesional. La usa el servicio de turnos antes de mostrar disponibilidad o reservar.
 */
@RestController
@RequestMapping("/api/professionals")
public class ProfessionalAgendaResource {

    private static final Logger LOG = LoggerFactory.getLogger(ProfessionalAgendaResource.class);

    private final ProfessionalAgendaService professionalAgendaService;

    public ProfessionalAgendaResource(ProfessionalAgendaService professionalAgendaService) {
        this.professionalAgendaService = professionalAgendaService;
    }

    /**
     * {@code GET /professionals/:id/agenda} : agenda vigente del profesional.
     *
     * @param id id local del profesional.
     * @return 200 con la agenda, o 404 si el profesional no existe en la copia local.
     */
    @GetMapping("/{id}/agenda")
    public ResponseEntity<ProfessionalAgendaDTO> getAgenda(@PathVariable("id") Long id) {
        LOG.debug("REST request to get agenda of Professional : {}", id);
        return ResponseUtil.wrapOrNotFound(professionalAgendaService.findAgenda(id));
    }
}

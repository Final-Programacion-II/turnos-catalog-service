package ar.edu.um.turnos.catalog.service;

import ar.edu.um.turnos.catalog.domain.Professional;
import ar.edu.um.turnos.catalog.repository.ProfessionalRepository;
import ar.edu.um.turnos.catalog.service.dto.ProfessionalDTO;
import ar.edu.um.turnos.catalog.service.mapper.ProfessionalMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ar.edu.um.turnos.catalog.domain.Professional}.
 */
@Service
@Transactional
public class ProfessionalService {

    private static final Logger LOG = LoggerFactory.getLogger(ProfessionalService.class);

    private final ProfessionalRepository professionalRepository;

    private final ProfessionalMapper professionalMapper;

    public ProfessionalService(ProfessionalRepository professionalRepository, ProfessionalMapper professionalMapper) {
        this.professionalRepository = professionalRepository;
        this.professionalMapper = professionalMapper;
    }

    /**
     * Save a professional.
     *
     * @param professionalDTO the entity to save.
     * @return the persisted entity.
     */
    public ProfessionalDTO save(ProfessionalDTO professionalDTO) {
        LOG.debug("Request to save Professional : {}", professionalDTO);
        Professional professional = professionalMapper.toEntity(professionalDTO);
        professional = professionalRepository.save(professional);
        return professionalMapper.toDto(professional);
    }

    /**
     * Update a professional.
     *
     * @param professionalDTO the entity to save.
     * @return the persisted entity.
     */
    public ProfessionalDTO update(ProfessionalDTO professionalDTO) {
        LOG.debug("Request to update Professional : {}", professionalDTO);
        Professional professional = professionalMapper.toEntity(professionalDTO);
        professional = professionalRepository.save(professional);
        return professionalMapper.toDto(professional);
    }

    /**
     * Partially update a professional.
     *
     * @param professionalDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ProfessionalDTO> partialUpdate(ProfessionalDTO professionalDTO) {
        LOG.debug("Request to partially update Professional : {}", professionalDTO);

        return professionalRepository
            .findById(professionalDTO.getId())
            .map(existingProfessional -> {
                professionalMapper.partialUpdate(existingProfessional, professionalDTO);

                return existingProfessional;
            })
            .map(professionalRepository::save)
            .map(professionalMapper::toDto);
    }

    /**
     * Get all the professionals.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<ProfessionalDTO> findAll() {
        LOG.debug("Request to get all Professionals");
        return professionalRepository.findAll().stream().map(professionalMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get all the professionals with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ProfessionalDTO> findAllWithEagerRelationships(Pageable pageable) {
        return professionalRepository.findAllWithEagerRelationships(pageable).map(professionalMapper::toDto);
    }

    /**
     * Get one professional by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ProfessionalDTO> findOne(Long id) {
        LOG.debug("Request to get Professional : {}", id);
        return professionalRepository.findOneWithEagerRelationships(id).map(professionalMapper::toDto);
    }

    /**
     * Delete the professional by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Professional : {}", id);
        professionalRepository.deleteById(id);
    }
}

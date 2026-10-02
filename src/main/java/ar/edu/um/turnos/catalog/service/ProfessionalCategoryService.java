package ar.edu.um.turnos.catalog.service;

import ar.edu.um.turnos.catalog.domain.ProfessionalCategory;
import ar.edu.um.turnos.catalog.repository.ProfessionalCategoryRepository;
import ar.edu.um.turnos.catalog.service.dto.ProfessionalCategoryDTO;
import ar.edu.um.turnos.catalog.service.mapper.ProfessionalCategoryMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ar.edu.um.turnos.catalog.domain.ProfessionalCategory}.
 */
@Service
@Transactional
public class ProfessionalCategoryService {

    private static final Logger LOG = LoggerFactory.getLogger(ProfessionalCategoryService.class);

    private final ProfessionalCategoryRepository professionalCategoryRepository;

    private final ProfessionalCategoryMapper professionalCategoryMapper;

    public ProfessionalCategoryService(
        ProfessionalCategoryRepository professionalCategoryRepository,
        ProfessionalCategoryMapper professionalCategoryMapper
    ) {
        this.professionalCategoryRepository = professionalCategoryRepository;
        this.professionalCategoryMapper = professionalCategoryMapper;
    }

    /**
     * Save a professionalCategory.
     *
     * @param professionalCategoryDTO the entity to save.
     * @return the persisted entity.
     */
    public ProfessionalCategoryDTO save(ProfessionalCategoryDTO professionalCategoryDTO) {
        LOG.debug("Request to save ProfessionalCategory : {}", professionalCategoryDTO);
        ProfessionalCategory professionalCategory = professionalCategoryMapper.toEntity(professionalCategoryDTO);
        professionalCategory = professionalCategoryRepository.save(professionalCategory);
        return professionalCategoryMapper.toDto(professionalCategory);
    }

    /**
     * Update a professionalCategory.
     *
     * @param professionalCategoryDTO the entity to save.
     * @return the persisted entity.
     */
    public ProfessionalCategoryDTO update(ProfessionalCategoryDTO professionalCategoryDTO) {
        LOG.debug("Request to update ProfessionalCategory : {}", professionalCategoryDTO);
        ProfessionalCategory professionalCategory = professionalCategoryMapper.toEntity(professionalCategoryDTO);
        professionalCategory = professionalCategoryRepository.save(professionalCategory);
        return professionalCategoryMapper.toDto(professionalCategory);
    }

    /**
     * Partially update a professionalCategory.
     *
     * @param professionalCategoryDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ProfessionalCategoryDTO> partialUpdate(ProfessionalCategoryDTO professionalCategoryDTO) {
        LOG.debug("Request to partially update ProfessionalCategory : {}", professionalCategoryDTO);

        return professionalCategoryRepository
            .findById(professionalCategoryDTO.getId())
            .map(existingProfessionalCategory -> {
                professionalCategoryMapper.partialUpdate(existingProfessionalCategory, professionalCategoryDTO);

                return existingProfessionalCategory;
            })
            .map(professionalCategoryRepository::save)
            .map(professionalCategoryMapper::toDto);
    }

    /**
     * Get all the professionalCategories.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<ProfessionalCategoryDTO> findAll() {
        LOG.debug("Request to get all ProfessionalCategories");
        return professionalCategoryRepository
            .findAll()
            .stream()
            .map(professionalCategoryMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one professionalCategory by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ProfessionalCategoryDTO> findOne(Long id) {
        LOG.debug("Request to get ProfessionalCategory : {}", id);
        return professionalCategoryRepository.findById(id).map(professionalCategoryMapper::toDto);
    }

    /**
     * Delete the professionalCategory by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete ProfessionalCategory : {}", id);
        professionalCategoryRepository.deleteById(id);
    }
}

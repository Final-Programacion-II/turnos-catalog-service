package ar.edu.um.turnos.catalog.service;

import ar.edu.um.turnos.catalog.repository.ProfessionalCategoryRepository;
import ar.edu.um.turnos.catalog.service.dto.ProfessionalCategoryDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta de categorías sobre la copia local. Es de solo lectura: las categorías se crean y
 * se modifican solo con la sincronización.
 */
@Service
@Transactional(readOnly = true)
public class ProfessionalCategoryService {

    private final ProfessionalCategoryRepository professionalCategoryRepository;

    public ProfessionalCategoryService(ProfessionalCategoryRepository professionalCategoryRepository) {
        this.professionalCategoryRepository = professionalCategoryRepository;
    }

    public List<ProfessionalCategoryDTO> findAll() {
        return professionalCategoryRepository.findAll().stream().map(ProfessionalCategoryDTO::from).toList();
    }

    public Optional<ProfessionalCategoryDTO> findOne(Long id) {
        return professionalCategoryRepository.findById(id).map(ProfessionalCategoryDTO::from);
    }
}

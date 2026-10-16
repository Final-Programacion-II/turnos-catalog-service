package ar.edu.um.turnos.catalog.repository;

import ar.edu.um.turnos.catalog.domain.ProfessionalCategory;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ProfessionalCategory entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProfessionalCategoryRepository extends JpaRepository<ProfessionalCategory, Long> {}

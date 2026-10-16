package ar.edu.um.turnos.catalog.repository;

import ar.edu.um.turnos.catalog.domain.Professional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Professional entity.
 */
@Repository
public interface ProfessionalRepository extends JpaRepository<Professional, Long> {
    default Optional<Professional> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Professional> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Professional> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select professional from Professional professional left join fetch professional.category",
        countQuery = "select count(professional) from Professional professional"
    )
    Page<Professional> findAllWithToOneRelationships(Pageable pageable);

    @Query("select professional from Professional professional left join fetch professional.category")
    List<Professional> findAllWithToOneRelationships();

    @Query("select professional from Professional professional left join fetch professional.category where professional.id =:id")
    Optional<Professional> findOneWithToOneRelationships(@Param("id") Long id);
}

package ar.edu.um.turnos.catalog.repository;

import ar.edu.um.turnos.catalog.domain.CatalogSyncState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatalogSyncStateRepository extends JpaRepository<CatalogSyncState, Long> {}

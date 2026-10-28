package ar.edu.um.turnos.catalog.sync.ports.out;

import ar.edu.um.turnos.catalog.sync.domain.CatalogChange;
import ar.edu.um.turnos.catalog.sync.domain.CatalogSnapshot;
import java.util.Optional;

public interface CatalogReplica {
    Optional<Long> currentVersion();

    void apply(CatalogSnapshot snapshot);

    void applyChange(CatalogChange change);
}

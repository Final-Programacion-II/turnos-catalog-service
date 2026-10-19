package ar.edu.um.turnos.catalog.sync.ports.out;

import ar.edu.um.turnos.catalog.sync.domain.CatalogSnapshot;
import java.util.Optional;

public interface CatalogReplica {
    Optional<Long> currentVersion();

    void apply(CatalogSnapshot snapshot);
}

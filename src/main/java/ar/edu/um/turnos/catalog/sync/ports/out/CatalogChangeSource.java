package ar.edu.um.turnos.catalog.sync.ports.out;

import ar.edu.um.turnos.catalog.sync.domain.CatalogChange;
import ar.edu.um.turnos.catalog.sync.domain.CatalogVersions;

public interface CatalogChangeSource {
    CatalogVersions versions();

    CatalogChange fetchChange(long version);
}

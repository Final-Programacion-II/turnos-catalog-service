package ar.edu.um.turnos.catalog.sync.ports.out;

import ar.edu.um.turnos.catalog.sync.domain.CatalogSnapshot;

public interface CatalogSnapshotSource {
    CatalogSnapshot fetchSnapshot();
}

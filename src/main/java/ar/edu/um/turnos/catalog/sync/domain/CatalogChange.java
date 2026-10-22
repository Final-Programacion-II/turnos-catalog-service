package ar.edu.um.turnos.catalog.sync.domain;

import java.util.List;

// Una versión incremental: las entidades que cambiaron, con su estado actual.
public record CatalogChange(
    long version,
    List<CatalogSnapshot.Category> categories,
    List<CatalogSnapshot.Professional> professionals,
    List<CatalogSnapshot.Schedule> schedules
) {}

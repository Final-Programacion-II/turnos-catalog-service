package ar.edu.um.turnos.catalog.service.dto;

import ar.edu.um.turnos.catalog.repository.entity.ProfessionalCategory;
import java.time.Instant;

/**
 * Una categoría de la copia local, tal como la devuelve la API.
 */
public record ProfessionalCategoryDTO(
    Long id,
    Long externalId,
    String name,
    String description,
    Boolean enabled,
    Instant createdAt,
    Instant updatedAt
) {
    public static ProfessionalCategoryDTO from(ProfessionalCategory category) {
        return new ProfessionalCategoryDTO(
            category.getId(),
            category.getExternalId(),
            category.getName(),
            category.getDescription(),
            category.getEnabled(),
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}

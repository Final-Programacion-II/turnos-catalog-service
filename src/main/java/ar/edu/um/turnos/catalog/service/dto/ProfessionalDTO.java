package ar.edu.um.turnos.catalog.service.dto;

import ar.edu.um.turnos.catalog.repository.entity.Professional;
import ar.edu.um.turnos.catalog.repository.entity.ProfessionalCategory;
import java.time.Instant;

/**
 * Un profesional de la copia local, tal como lo devuelve la API.
 */
public record ProfessionalDTO(
    Long id,
    Long externalId,
    String firstName,
    String lastName,
    Boolean enabled,
    Instant createdAt,
    Instant updatedAt,
    Category category
) {
    public static ProfessionalDTO from(Professional professional) {
        return new ProfessionalDTO(
            professional.getId(),
            professional.getExternalId(),
            professional.getFirstName(),
            professional.getLastName(),
            professional.getEnabled(),
            professional.getCreatedAt(),
            professional.getUpdatedAt(),
            Category.from(professional.getCategory())
        );
    }

    /**
     * De la categoría va solo lo necesario para mostrarla junto al profesional. El detalle está
     * en /api/professional-categories.
     */
    public record Category(Long id, String name) {
        static Category from(ProfessionalCategory category) {
            return category == null ? null : new Category(category.getId(), category.getName());
        }
    }
}

package ar.edu.um.turnos.catalog.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link ar.edu.um.turnos.catalog.domain.ProfessionalCategory} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ProfessionalCategoryDTO implements Serializable {

    private Long id;

    @NotNull
    private Long externalId;

    @NotNull
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull
    private Boolean enabled;

    private Instant createdAt;

    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExternalId() {
        return externalId;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProfessionalCategoryDTO)) {
            return false;
        }

        ProfessionalCategoryDTO professionalCategoryDTO = (ProfessionalCategoryDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, professionalCategoryDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ProfessionalCategoryDTO{" +
            "id=" + getId() +
            ", externalId=" + getExternalId() +
            ", name='" + getName() + "'" +
            ", description='" + getDescription() + "'" +
            ", enabled='" + getEnabled() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
}

package ar.edu.um.turnos.catalog.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link ar.edu.um.turnos.catalog.domain.Professional} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ProfessionalDTO implements Serializable {

    private Long id;

    @NotNull
    private Long externalId;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    private Boolean enabled;

    private Instant createdAt;

    private Instant updatedAt;

    @NotNull
    private ProfessionalCategoryDTO category;

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public ProfessionalCategoryDTO getCategory() {
        return category;
    }

    public void setCategory(ProfessionalCategoryDTO category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProfessionalDTO)) {
            return false;
        }

        ProfessionalDTO professionalDTO = (ProfessionalDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, professionalDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ProfessionalDTO{" +
            "id=" + getId() +
            ", externalId=" + getExternalId() +
            ", firstName='" + getFirstName() + "'" +
            ", lastName='" + getLastName() + "'" +
            ", enabled='" + getEnabled() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", category=" + getCategory() +
            "}";
    }
}

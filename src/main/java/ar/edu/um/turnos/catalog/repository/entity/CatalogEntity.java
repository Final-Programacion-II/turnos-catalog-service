package ar.edu.um.turnos.catalog.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * Lo que tienen en común las tres entidades que se copian de la cátedra: el id que les da la
 * cátedra, si están habilitadas y sus fechas. Nada se borra de la copia: lo que la cátedra da
 * de baja queda con {@code enabled = false}.
 */
@MappedSuperclass
public abstract class CatalogEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Column(name = "external_id", nullable = false, unique = true)
    private Long externalId;

    @NotNull
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Id local, el de la base del catálogo. El de la cátedra es {@link #getExternalId()}.
     */
    public abstract Long getId();

    public Long getExternalId() {
        return externalId;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
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
}

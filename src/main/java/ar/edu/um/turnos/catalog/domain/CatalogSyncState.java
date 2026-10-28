package ar.edu.um.turnos.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;

/**
 * Estado de la réplica local del catálogo de la cátedra.
 * La tabla tiene una sola fila, con id fijo {@link #SINGLETON_ID}.
 */
@Entity
@Table(name = "catalog_sync_state")
public class CatalogSyncState implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Long SINGLETON_ID = 1L;

    @Id
    @Column(name = "id")
    private Long id;

    /**
     * Última versión del catálogo aplicada por completo. Null si nunca se sincronizó.
     */
    @Column(name = "catalog_version")
    private Long catalogVersion;

    /**
     * Última sincronización que terminó bien (aunque no haya habido cambios).
     */
    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    /**
     * Último intento de sincronizar, haya salido bien o mal.
     */
    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    /**
     * Error del último intento. Null si el último intento salió bien.
     */
    @Column(name = "last_error", length = 1000)
    private String lastError;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCatalogVersion() {
        return catalogVersion;
    }

    public void setCatalogVersion(Long catalogVersion) {
        this.catalogVersion = catalogVersion;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public Instant getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(Instant lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    @Override
    public String toString() {
        return (
            "CatalogSyncState{catalogVersion=" +
            catalogVersion +
            ", lastSyncedAt=" +
            lastSyncedAt +
            ", lastAttemptAt=" +
            lastAttemptAt +
            ", lastError=" +
            lastError +
            "}"
        );
    }
}

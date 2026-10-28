package ar.edu.um.turnos.catalog.service.dto;

import java.time.Instant;

/**
 * Estado de la sincronización del catálogo.
 * status vale NOT_SYNCED si todavía no hay versión local, ERROR si el último intento falló, y OK en otro caso.
 */
public record CatalogSyncStatusDTO(String status, Long catalogVersion, Instant lastSyncedAt, Instant lastAttemptAt, String lastError) {}

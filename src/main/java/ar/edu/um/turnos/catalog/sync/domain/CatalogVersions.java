package ar.edu.um.turnos.catalog.sync.domain;

// Versiones que publica la cátedra en Redis.
public record CatalogVersions(long current, long oldestAvailable) {
    public boolean isUpToDate(long localVersion) {
        return localVersion >= current;
    }

    // Si la versión local es más vieja que la ventana disponible, faltan cambios y hay que pedir el snapshot.
    public boolean canContinueFrom(long localVersion) {
        return localVersion >= oldestAvailable;
    }
}

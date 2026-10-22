package ar.edu.um.turnos.catalog.sync.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.um.turnos.catalog.sync.domain.CatalogChange;
import ar.edu.um.turnos.catalog.sync.domain.CatalogSnapshot;
import ar.edu.um.turnos.catalog.sync.domain.CatalogVersions;
import ar.edu.um.turnos.catalog.sync.ports.out.CatalogChangeSource;
import ar.edu.um.turnos.catalog.sync.ports.out.CatalogReplica;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CatalogSyncServiceTest {

    private final CatalogSnapshot snapshot = new CatalogSnapshot(7L, List.of(), List.of(), List.of());

    @Test
    void sinVersionCargaElSnapshot() {
        ReplicaFalsa replica = new ReplicaFalsa(null);
        CatalogSyncService service = new CatalogSyncService(() -> snapshot, new CambiosFalsos(7, 4), replica);

        assertThat(service.synchronize()).isEqualTo(7L);
        assertThat(replica.snapshotAplicado).isSameAs(snapshot);
    }

    @Test
    void alDiaNoHaceNada() {
        ReplicaFalsa replica = new ReplicaFalsa(7L);
        CatalogSyncService service = new CatalogSyncService(() -> snapshot, new CambiosFalsos(7, 4), replica);

        assertThat(service.synchronize()).isEqualTo(7L);
        assertThat(replica.snapshotAplicado).isNull();
        assertThat(replica.cambiosAplicados).isEmpty();
    }

    @Test
    void aplicaLosCambiosEnOrden() {
        ReplicaFalsa replica = new ReplicaFalsa(4L);
        CatalogSyncService service = new CatalogSyncService(() -> snapshot, new CambiosFalsos(7, 4), replica);

        assertThat(service.synchronize()).isEqualTo(7L);
        assertThat(replica.cambiosAplicados).containsExactly(5L, 6L, 7L);
        assertThat(replica.snapshotAplicado).isNull();
    }

    // Caso de la sección 18.2: local 3, más vieja disponible 4.
    @Test
    void siFaltaUnaVersionCargaElSnapshot() {
        ReplicaFalsa replica = new ReplicaFalsa(3L);
        CatalogSyncService service = new CatalogSyncService(() -> snapshot, new CambiosFalsos(7, 4), replica);

        assertThat(service.synchronize()).isEqualTo(7L);
        assertThat(replica.snapshotAplicado).isSameAs(snapshot);
        assertThat(replica.cambiosAplicados).isEmpty();
    }

    @Test
    void unSnapshotRotoNoSeAplica() {
        CatalogSnapshot roto = new CatalogSnapshot(
            7L,
            List.of(),
            List.of(new CatalogSnapshot.Professional(15L, 99L, "Ana", "Gomez", true, null, null)),
            List.of()
        );
        ReplicaFalsa replica = new ReplicaFalsa(null);
        CatalogSyncService service = new CatalogSyncService(() -> roto, new CambiosFalsos(7, 4), replica);

        assertThatThrownBy(service::synchronize).isInstanceOf(IllegalArgumentException.class);
        assertThat(replica.snapshotAplicado).isNull();
    }

    private static class CambiosFalsos implements CatalogChangeSource {

        private final CatalogVersions versions;

        CambiosFalsos(long actual, long masViejaDisponible) {
            this.versions = new CatalogVersions(actual, masViejaDisponible);
        }

        @Override
        public CatalogVersions versions() {
            return versions;
        }

        @Override
        public CatalogChange fetchChange(long version) {
            return new CatalogChange(version, List.of(), List.of(), List.of());
        }
    }

    private static class ReplicaFalsa implements CatalogReplica {

        private final Long version;
        private CatalogSnapshot snapshotAplicado;
        private final List<Long> cambiosAplicados = new ArrayList<>();

        ReplicaFalsa(Long version) {
            this.version = version;
        }

        @Override
        public Optional<Long> currentVersion() {
            return Optional.ofNullable(version);
        }

        @Override
        public void apply(CatalogSnapshot snapshot) {
            snapshotAplicado = snapshot;
        }

        @Override
        public void applyChange(CatalogChange change) {
            cambiosAplicados.add(change.version());
        }
    }
}

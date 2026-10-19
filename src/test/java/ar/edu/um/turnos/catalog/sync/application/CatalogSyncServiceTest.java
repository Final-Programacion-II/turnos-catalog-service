package ar.edu.um.turnos.catalog.sync.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.um.turnos.catalog.sync.domain.CatalogSnapshot;
import ar.edu.um.turnos.catalog.sync.ports.out.CatalogReplica;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CatalogSyncServiceTest {

    @Test
    void sinVersionTraeElSnapshotYLoAplica() {
        CatalogSnapshot snapshot = new CatalogSnapshot(7L, List.of(), List.of(), List.of());
        ReplicaFalsa replica = new ReplicaFalsa(null);
        CatalogSyncService service = new CatalogSyncService(() -> snapshot, replica);

        long version = service.synchronize();

        assertThat(version).isEqualTo(7L);
        assertThat(replica.aplicado).isSameAs(snapshot);
    }

    @Test
    void conVersionNoHaceNada() {
        ReplicaFalsa replica = new ReplicaFalsa(7L);
        CatalogSyncService service = new CatalogSyncService(() -> {
            throw new AssertionError("No debería pedir el snapshot");
        }, replica);

        long version = service.synchronize();

        assertThat(version).isEqualTo(7L);
        assertThat(replica.aplicado).isNull();
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
        CatalogSyncService service = new CatalogSyncService(() -> roto, replica);

        assertThatThrownBy(service::synchronize).isInstanceOf(IllegalArgumentException.class);
        assertThat(replica.aplicado).isNull();
    }

    private static class ReplicaFalsa implements CatalogReplica {

        private final Long version;
        private CatalogSnapshot aplicado;

        ReplicaFalsa(Long version) {
            this.version = version;
        }

        @Override
        public Optional<Long> currentVersion() {
            return Optional.ofNullable(version);
        }

        @Override
        public void apply(CatalogSnapshot snapshot) {
            aplicado = snapshot;
        }
    }
}

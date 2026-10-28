package ar.edu.um.turnos.catalog.sync.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.um.turnos.catalog.IntegrationTest;
import ar.edu.um.turnos.catalog.domain.CatalogSyncState;
import ar.edu.um.turnos.catalog.domain.Professional;
import ar.edu.um.turnos.catalog.domain.ProfessionalCategory;
import ar.edu.um.turnos.catalog.repository.CatalogSyncStateRepository;
import ar.edu.um.turnos.catalog.repository.ProfessionalCategoryRepository;
import ar.edu.um.turnos.catalog.repository.ProfessionalRepository;
import ar.edu.um.turnos.catalog.repository.WeeklyScheduleRepository;
import ar.edu.um.turnos.catalog.sync.domain.CatalogChange;
import ar.edu.um.turnos.catalog.sync.domain.CatalogSnapshot;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

// Sin @Transactional: el adaptador maneja su propia transacción y eso es lo que se prueba.
@IntegrationTest
class JpaCatalogReplicaIT {

    @Autowired
    private JpaCatalogReplica replica;

    @Autowired
    private ProfessionalCategoryRepository categoryRepository;

    @Autowired
    private ProfessionalRepository professionalRepository;

    @Autowired
    private WeeklyScheduleRepository scheduleRepository;

    @Autowired
    private CatalogSyncStateRepository syncStateRepository;

    @BeforeEach
    @AfterEach
    void limpiar() {
        scheduleRepository.deleteAll();
        professionalRepository.deleteAll();
        categoryRepository.deleteAll();
        CatalogSyncState state = syncStateRepository.findById(CatalogSyncState.SINGLETON_ID).orElseThrow();
        state.setCatalogVersion(null);
        state.setLastSyncedAt(null);
        syncStateRepository.save(state);
    }

    @Test
    void baseVaciaSinVersion() {
        assertThat(replica.currentVersion()).isEmpty();
    }

    @Test
    void aplicaElSnapshot() {
        replica.apply(snapshotBase());

        assertThat(categoryRepository.count()).isEqualTo(1);
        assertThat(professionalRepository.count()).isEqualTo(1);
        assertThat(scheduleRepository.count()).isEqualTo(1);
        assertThat(replica.currentVersion()).contains(7L);
    }

    @Test
    void aplicarDosVecesNoDuplica() {
        replica.apply(snapshotBase());
        replica.apply(snapshotBase());

        assertThat(categoryRepository.count()).isEqualTo(1);
        assertThat(professionalRepository.count()).isEqualTo(1);
        assertThat(scheduleRepository.count()).isEqualTo(1);
    }

    @Test
    void actualizaYDeshabilitaLoQueFalta() {
        replica.apply(new CatalogSnapshot(7L, List.of(categoria(1L, "Clinica"), categoria(2L, "Pediatria")), List.of(), List.of()));
        replica.apply(new CatalogSnapshot(8L, List.of(categoria(1L, "Clinica general")), List.of(), List.of()));

        ProfessionalCategory uno = buscarCategoria(1L);
        ProfessionalCategory dos = buscarCategoria(2L);
        assertThat(uno.getName()).isEqualTo("Clinica general");
        assertThat(uno.getEnabled()).isTrue();
        assertThat(dos.getEnabled()).isFalse();
        assertThat(replica.currentVersion()).contains(8L);
    }

    @Test
    void conUnErrorNoGuardaNada() {
        CatalogSnapshot roto = new CatalogSnapshot(7L, List.of(categoria(1L, "Clinica")), List.of(profesional(15L, 99L, true)), List.of());

        assertThatThrownBy(() -> replica.apply(roto)).isInstanceOf(IllegalStateException.class);

        assertThat(categoryRepository.count()).isZero();
        assertThat(replica.currentVersion()).isEmpty();
    }

    // El cambio trae solo el profesional; su categoría ya estaba en la base.
    @Test
    void aplicaUnCambioSobreLaVersionAnterior() {
        replica.apply(snapshotBase());

        replica.applyChange(new CatalogChange(8L, List.of(), List.of(profesional(15L, 1L, false)), List.of()));

        assertThat(professionalRepository.count()).isEqualTo(1);
        Professional profesional = professionalRepository.findAll().get(0);
        assertThat(profesional.getEnabled()).isFalse();
        assertThat(replica.currentVersion()).contains(8L);
    }

    @Test
    void unCambioNoDeshabilitaLoQueNoViene() {
        replica.apply(new CatalogSnapshot(7L, List.of(categoria(1L, "Clinica"), categoria(2L, "Pediatria")), List.of(), List.of()));

        replica.applyChange(new CatalogChange(8L, List.of(categoria(1L, "Clinica general")), List.of(), List.of()));

        assertThat(buscarCategoria(1L).getName()).isEqualTo("Clinica general");
        assertThat(buscarCategoria(2L).getEnabled()).isTrue();
    }

    @Test
    void reaplicarUnCambioNoHaceNada() {
        replica.apply(snapshotBase());
        CatalogChange cambio = new CatalogChange(8L, List.of(categoria(1L, "Clinica general")), List.of(), List.of());

        replica.applyChange(cambio);
        replica.applyChange(cambio);

        assertThat(categoryRepository.count()).isEqualTo(1);
        assertThat(replica.currentVersion()).contains(8L);
    }

    @Test
    void unCambioFueraDeOrdenFalla() {
        replica.apply(snapshotBase());

        assertThatThrownBy(() -> replica.applyChange(new CatalogChange(9L, List.of(), List.of(), List.of()))).isInstanceOf(
            IllegalStateException.class
        );
        assertThat(replica.currentVersion()).contains(7L);
    }

    private CatalogSnapshot snapshotBase() {
        return new CatalogSnapshot(7L, List.of(categoria(1L, "Clinica")), List.of(profesional(15L, 1L, true)), List.of(horario(21L, 15L)));
    }

    private ProfessionalCategory buscarCategoria(Long externalId) {
        for (ProfessionalCategory c : categoryRepository.findAll()) {
            if (c.getExternalId().equals(externalId)) {
                return c;
            }
        }
        throw new AssertionError("No existe la categoría " + externalId);
    }

    private CatalogSnapshot.Category categoria(Long id, String nombre) {
        return new CatalogSnapshot.Category(id, nombre, null, true, null, null);
    }

    private CatalogSnapshot.Professional profesional(Long id, Long categoriaId, boolean habilitado) {
        return new CatalogSnapshot.Professional(id, categoriaId, "Ana", "Gomez", habilitado, null, null);
    }

    private CatalogSnapshot.Schedule horario(Long id, Long profesionalId) {
        return new CatalogSnapshot.Schedule(
            id,
            profesionalId,
            DayOfWeek.MONDAY,
            LocalTime.of(9, 0),
            LocalTime.of(12, 0),
            30,
            true,
            null,
            null
        );
    }
}

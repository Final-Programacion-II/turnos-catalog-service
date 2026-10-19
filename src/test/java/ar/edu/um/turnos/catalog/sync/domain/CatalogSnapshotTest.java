package ar.edu.um.turnos.catalog.sync.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class CatalogSnapshotTest {

    private final CatalogSnapshot.Category categoria = new CatalogSnapshot.Category(1L, "Clinica medica", null, true, null, null);

    @Test
    void referenciasCorrectas() {
        CatalogSnapshot snapshot = new CatalogSnapshot(7L, List.of(categoria), List.of(profesional(15L, 1L)), List.of(horario(21L, 15L)));

        assertThatCode(snapshot::validateReferences).doesNotThrowAnyException();
    }

    @Test
    void profesionalConCategoriaInexistente() {
        CatalogSnapshot snapshot = new CatalogSnapshot(7L, List.of(categoria), List.of(profesional(15L, 99L)), List.of());

        assertThatThrownBy(snapshot::validateReferences).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void horarioConProfesionalInexistente() {
        CatalogSnapshot snapshot = new CatalogSnapshot(7L, List.of(categoria), List.of(), List.of(horario(21L, 15L)));

        assertThatThrownBy(snapshot::validateReferences).isInstanceOf(IllegalArgumentException.class);
    }

    private CatalogSnapshot.Professional profesional(Long id, Long categoriaId) {
        return new CatalogSnapshot.Professional(id, categoriaId, "Ana", "Gomez", true, null, null);
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

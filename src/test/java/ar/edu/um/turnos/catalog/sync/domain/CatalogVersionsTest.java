package ar.edu.um.turnos.catalog.sync.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CatalogVersionsTest {

    private final CatalogVersions versiones = new CatalogVersions(7, 4);

    @Test
    void alDiaSiLaVersionLocalEsLaActual() {
        assertThat(versiones.isUpToDate(7)).isTrue();
        assertThat(versiones.isUpToDate(6)).isFalse();
    }

    @Test
    void puedeContinuarDentroDeLaVentana() {
        assertThat(versiones.canContinueFrom(4)).isTrue();
        assertThat(versiones.canContinueFrom(6)).isTrue();
    }

    // Ejemplo de la sección 18.2 de la referencia: local 3, actual 7, más vieja disponible 4.
    @Test
    void noPuedeContinuarSiFaltaUnaVersion() {
        assertThat(versiones.canContinueFrom(3)).isFalse();
    }
}

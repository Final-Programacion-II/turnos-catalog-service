package ar.edu.um.turnos.catalog.sync.infrastructure.status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

class RecordingCatalogSyncTest {

    private final SyncStatusRecorder status = mock(SyncStatusRecorder.class);

    @Test
    void siSaleBienGuardaElExito() {
        RecordingCatalogSync sync = new RecordingCatalogSync(() -> 7L, status);

        assertThat(sync.synchronize()).isEqualTo(7L);
        verify(status).recordSuccess();
        verify(status, never()).recordFailure(any());
    }

    @Test
    void siFallaGuardaElErrorYLoVuelveATirar() {
        IllegalStateException error = new IllegalStateException("Redis no responde");
        RecordingCatalogSync sync = new RecordingCatalogSync(() -> {
            throw error;
        }, status);

        assertThatThrownBy(sync::synchronize).isSameAs(error);
        verify(status).recordFailure(error);
        verify(status, never()).recordSuccess();
    }
}

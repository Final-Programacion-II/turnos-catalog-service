package ar.edu.um.turnos.catalog.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.um.turnos.catalog.IntegrationTest;
import ar.edu.um.turnos.catalog.domain.CatalogSyncState;
import ar.edu.um.turnos.catalog.repository.CatalogSyncStateRepository;
import ar.edu.um.turnos.catalog.repository.ProfessionalCategoryRepository;
import ar.edu.um.turnos.catalog.repository.ProfessionalRepository;
import ar.edu.um.turnos.catalog.repository.WeeklyScheduleRepository;
import ar.edu.um.turnos.catalog.sync.ports.in.SynchronizeCatalogUseCase;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

// Sincronización completa contra un servidor falso que imita a la cátedra.
@IntegrationTest
class CatalogSyncIT {

    private static final String JSON = """
        {
          "snapshotVersion": 7,
          "professionalCategories": [
            { "id": 1, "name": "Clinica medica", "description": null, "enabled": true }
          ],
          "professionals": [
            { "id": 15, "categoryId": 1, "firstName": "Ana", "lastName": "Gomez", "enabled": true }
          ],
          "weeklySchedules": [
            { "id": 21, "professionalId": 15, "dayOfWeek": "MONDAY", "startTime": "09:00:00", "endTime": "12:00:00",
              "slotDurationMinutes": 30, "enabled": true }
          ]
        }
        """;

    private static HttpServer catedra;
    private static int status = 200;
    private static String authorizationRecibido;

    @Autowired
    private SynchronizeCatalogUseCase synchronizeCatalog;

    @Autowired
    private ProfessionalCategoryRepository categoryRepository;

    @Autowired
    private ProfessionalRepository professionalRepository;

    @Autowired
    private WeeklyScheduleRepository scheduleRepository;

    @Autowired
    private CatalogSyncStateRepository syncStateRepository;

    @DynamicPropertySource
    static void configurarCatedra(DynamicPropertyRegistry registry) throws IOException {
        catedra = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        catedra.createContext("/api/synchronization/snapshot", exchange -> {
            authorizationRecibido = exchange.getRequestHeaders().getFirst("Authorization");
            byte[] body = (status == 200 ? JSON : "{}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        catedra.start();

        registry.add("catedra.base-url", () -> "http://localhost:" + catedra.getAddress().getPort());
        registry.add("catedra.token", () -> "token-prueba");
    }

    @AfterAll
    static void apagarCatedra() {
        catedra.stop(0);
    }

    // Al arrancar la app ya sincroniza sola, así que cada test empieza con la base vacía.
    @BeforeEach
    void limpiar() {
        status = 200;
        authorizationRecibido = null;
        scheduleRepository.deleteAll();
        professionalRepository.deleteAll();
        categoryRepository.deleteAll();
        CatalogSyncState state = syncStateRepository.findById(CatalogSyncState.SINGLETON_ID).orElseThrow();
        state.setCatalogVersion(null);
        state.setLastSyncedAt(null);
        syncStateRepository.save(state);
    }

    @Test
    void sincronizaDesdeLaCatedra() {
        long version = synchronizeCatalog.synchronize();

        assertThat(version).isEqualTo(7L);
        assertThat(authorizationRecibido).isEqualTo("Bearer token-prueba");
        assertThat(categoryRepository.count()).isEqualTo(1);
        assertThat(professionalRepository.count()).isEqualTo(1);
        assertThat(scheduleRepository.count()).isEqualTo(1);
    }

    @Test
    void siLaCatedraFallaNoGuardaNada() {
        status = 500;

        assertThatThrownBy(() -> synchronizeCatalog.synchronize()).isInstanceOf(RuntimeException.class);

        assertThat(categoryRepository.count()).isZero();
        assertThat(syncStateRepository.findById(CatalogSyncState.SINGLETON_ID).orElseThrow().getCatalogVersion()).isNull();
    }
}

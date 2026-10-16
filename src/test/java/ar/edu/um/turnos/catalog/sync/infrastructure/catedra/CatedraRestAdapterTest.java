package ar.edu.um.turnos.catalog.sync.infrastructure.catedra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.um.turnos.catalog.sync.domain.CatalogSnapshot;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class CatedraRestAdapterTest {

    private static final String JSON = """
        {
          "snapshotVersion": 7,
          "generatedAt": "2026-09-10T13:44:59Z",
          "professionalCategories": [
            { "id": 1, "name": "Clinica medica", "description": "Atencion clinica general", "enabled": true,
              "createdAt": "2026-07-01T12:00:00Z", "updatedAt": "2026-09-10T13:40:00Z" }
          ],
          "professionals": [
            { "id": 15, "categoryId": 1, "firstName": "Ana", "lastName": "Gomez", "enabled": true,
              "createdAt": "2026-07-01T12:00:00Z", "updatedAt": "2026-09-10T13:40:00Z" }
          ],
          "weeklySchedules": [
            { "id": 21, "professionalId": 15, "dayOfWeek": "MONDAY", "startTime": "09:00:00", "endTime": "12:00:00",
              "slotDurationMinutes": 30, "enabled": true,
              "createdAt": "2026-07-01T12:00:00Z", "updatedAt": "2026-09-10T13:40:00Z" }
          ]
        }
        """;

    private HttpServer server;
    private String authorizationRecibido;

    @AfterEach
    void apagarServidor() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void traeElSnapshotConElToken() throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/synchronization/snapshot", exchange -> {
            authorizationRecibido = exchange.getRequestHeaders().getFirst("Authorization");
            byte[] body = JSON.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();

        CatedraRestAdapter adapter = new CatedraRestAdapter(
            properties("http://localhost:" + server.getAddress().getPort(), "token-prueba")
        );
        CatalogSnapshot snapshot = adapter.fetchSnapshot();

        assertThat(authorizationRecibido).isEqualTo("Bearer token-prueba");
        assertThat(snapshot.version()).isEqualTo(7L);
        assertThat(snapshot.categories()).hasSize(1);
        assertThat(snapshot.professionals().get(0).categoryId()).isEqualTo(1L);
        assertThat(snapshot.schedules().get(0).dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(snapshot.schedules().get(0).startTime()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void sinTokenNoLlamaALaCatedra() {
        CatedraRestAdapter adapter = new CatedraRestAdapter(properties("http://localhost:8090", ""));

        assertThatThrownBy(adapter::fetchSnapshot).isInstanceOf(IllegalStateException.class).hasMessageContaining("CATEDRA_TOKEN");
    }

    private CatedraProperties properties(String url, String token) {
        return new CatedraProperties(url, token, Duration.ofSeconds(2), Duration.ofSeconds(2));
    }
}

package ar.edu.um.turnos.catalog.sync.infrastructure.catedra;

import ar.edu.um.turnos.catalog.sync.domain.CatalogSnapshot;
import ar.edu.um.turnos.catalog.sync.ports.out.CatalogSnapshotSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
@EnableConfigurationProperties(CatedraProperties.class)
public class CatedraRestAdapter implements CatalogSnapshotSource {

    private final CatedraProperties properties;
    private final RestClient restClient;

    public CatedraRestAdapter(CatedraProperties properties) {
        this.properties = properties;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());

        RestClient.Builder builder = RestClient.builder().requestFactory(requestFactory);
        if (StringUtils.hasText(properties.baseUrl())) {
            builder.baseUrl(properties.baseUrl());
        }
        if (StringUtils.hasText(properties.token())) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.token());
        }
        this.restClient = builder.build();
    }

    @Override
    public CatalogSnapshot fetchSnapshot() {
        if (!StringUtils.hasText(properties.baseUrl()) || !StringUtils.hasText(properties.token())) {
            throw new IllegalStateException("Falta configurar CATEDRA_BASE_URL o CATEDRA_TOKEN");
        }

        CatalogSnapshotResponse response = restClient
            .get()
            .uri("/api/synchronization/snapshot")
            .retrieve()
            .body(CatalogSnapshotResponse.class);
        if (response == null) {
            throw new IllegalStateException("La cátedra devolvió un snapshot vacío");
        }
        return response.toDomain();
    }
}

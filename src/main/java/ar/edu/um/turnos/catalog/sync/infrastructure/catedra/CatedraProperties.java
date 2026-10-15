package ar.edu.um.turnos.catalog.sync.infrastructure.catedra;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "catedra")
public record CatedraProperties(
    String baseUrl,
    String token,
    @DefaultValue("10s") Duration connectTimeout,
    @DefaultValue("30s") Duration readTimeout
) {}

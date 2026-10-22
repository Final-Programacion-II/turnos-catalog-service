package ar.edu.um.turnos.catalog.sync.infrastructure.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.um.turnos.catalog.sync.domain.CatalogChange;
import ar.edu.um.turnos.catalog.sync.domain.CatalogVersions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

class RedisCatalogChangeSourceIT {

    private static GenericContainer<?> redisContainer;
    private static LettuceConnectionFactory factory;
    private static StringRedisTemplate redis;

    private RedisCatalogChangeSource source;

    @BeforeAll
    static void levantarRedis() {
        redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine")).withExposedPorts(6379);
        redisContainer.start();
        factory = new LettuceConnectionFactory(redisContainer.getHost(), redisContainer.getMappedPort(6379));
        factory.afterPropertiesSet();
        redis = new StringRedisTemplate(factory);
    }

    @AfterAll
    static void apagarRedis() {
        factory.destroy();
        redisContainer.stop();
    }

    @BeforeEach
    void cargarDatos() {
        redis.getConnectionFactory().getConnection().serverCommands().flushAll();
        redis.opsForValue().set("catedra:sync:current-version", "7");
        redis.opsForValue().set("catedra:sync:oldest-available-version", "4");
        redis
            .opsForValue()
            .set(
                "catedra:sync:changes:7",
                "{\"version\":7,\"changes\":{\"professionalCategories\":[],\"professionals\":[15],\"weeklySchedules\":[]}}"
            );
        redis
            .opsForHash()
            .put(
                "catedra:sync:professionals",
                "15",
                "{\"id\":15,\"categoryId\":1,\"firstName\":\"Ana\",\"lastName\":\"Gomez\",\"enabled\":false}"
            );
        source = new RedisCatalogChangeSource(redis);
    }

    @Test
    void leeLasVersiones() {
        CatalogVersions versions = source.versions();

        assertThat(versions.current()).isEqualTo(7);
        assertThat(versions.oldestAvailable()).isEqualTo(4);
    }

    @Test
    void leeUnCambioConElEstadoActualDeLasEntidades() {
        CatalogChange change = source.fetchChange(7);

        assertThat(change.version()).isEqualTo(7);
        assertThat(change.categories()).isEmpty();
        assertThat(change.professionals()).hasSize(1);
        assertThat(change.professionals().get(0).id()).isEqualTo(15L);
        assertThat(change.professionals().get(0).enabled()).isFalse();
    }

    @Test
    void siNoEstaElCambioFalla() {
        assertThatThrownBy(() -> source.fetchChange(3)).isInstanceOf(IllegalStateException.class);
    }
}

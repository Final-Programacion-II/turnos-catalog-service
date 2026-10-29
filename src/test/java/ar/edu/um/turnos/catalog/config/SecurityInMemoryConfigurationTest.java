package ar.edu.um.turnos.catalog.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class SecurityInMemoryConfigurationTest {

    private final PasswordEncoder encoder = new SecurityInMemoryConfiguration().passwordEncoder();

    @Test
    void guardaLasContraseniasConArgon2id() {
        String hash = encoder.encode("admin");

        // El hash lleva el algoritmo y sus parámetros: $argon2id$v=19$m=16384,t=2,p=1$<salt>$<hash>
        assertThat(hash).startsWith("$argon2id$").contains("m=16384,t=2,p=1");
        assertThat(encoder.matches("admin", hash)).isTrue();
        assertThat(encoder.matches("otra", hash)).isFalse();
    }

    @Test
    void laMismaContraseniaDaHashesDistintos() {
        // Cada hash usa un salt aleatorio: dos usuarios con la misma contraseña no quedan iguales en la base
        assertThat(encoder.encode("admin")).isNotEqualTo(encoder.encode("admin"));
    }
}

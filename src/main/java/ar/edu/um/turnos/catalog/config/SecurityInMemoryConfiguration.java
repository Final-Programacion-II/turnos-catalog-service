package ar.edu.um.turnos.catalog.config;

import java.util.List;
import org.springframework.boot.security.autoconfigure.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.util.StringUtils;

@Configuration
public class SecurityInMemoryConfiguration {

    // Argon2id en lugar de bcrypt: además de ser lento, obliga a usar memoria en cada intento,
    // y eso limita cuántas contraseñas puede probar en paralelo una GPU (ver docs/decisiones.md, punto 7).
    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager(SecurityProperties properties, PasswordEncoder passwordEncoder) {
        SecurityProperties.User user = properties.getUser();
        List<String> roles = user.getRoles();
        return new InMemoryUserDetailsManager(
            User.withUsername(user.getName())
                .password(passwordEncoder.encode(user.getPassword()))
                .roles(StringUtils.toStringArray(roles))
                .build()
        );
    }
}

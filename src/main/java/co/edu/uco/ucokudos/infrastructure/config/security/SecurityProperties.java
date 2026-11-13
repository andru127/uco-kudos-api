package co.edu.uco.ucokudos.infrastructure.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "kudos.security")
public record SecurityProperties(String jwtSecret, Duration tokenTtl, List<String> corsAllowedOrigins) {

    private static final int MIN_SECRET_BYTES = 32;

    public SecurityProperties {
        if (jwtSecret == null || jwtSecret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres (HS256 exige 256 bits)");
        }
        tokenTtl = tokenTtl == null ? Duration.ofHours(8) : tokenTtl;
        corsAllowedOrigins = corsAllowedOrigins == null ? List.of() : List.copyOf(corsAllowedOrigins);
    }
}

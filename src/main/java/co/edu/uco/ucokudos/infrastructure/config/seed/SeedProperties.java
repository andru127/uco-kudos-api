package co.edu.uco.ucokudos.infrastructure.config.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kudos.seed")
public record SeedProperties(boolean enabled, String demoPassword, String recipientEmail, String recipientPhone) {
}

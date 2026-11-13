package co.edu.uco.ucokudos.infrastructure.config.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Usuarios de demostración. recipientEmail/recipientPhone permiten que el estudiante de demo tenga
 * TU correo y TU WhatsApp para evidenciar las notificaciones en la sustentación.
 */
@ConfigurationProperties(prefix = "kudos.seed")
public record SeedProperties(boolean enabled, String demoPassword, String recipientEmail, String recipientPhone) {
}

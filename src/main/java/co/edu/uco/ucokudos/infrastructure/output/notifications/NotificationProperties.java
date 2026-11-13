package co.edu.uco.ucokudos.infrastructure.output.notifications;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración externa de notificaciones (factor III de Twelve-Factor: config en el entorno).
 * Ningún secreto vive en el código; todo llega por variables de entorno.
 */
@ConfigurationProperties(prefix = "kudos.notifications")
public record NotificationProperties(Email email, WhatsApp whatsapp, Retry retry, String frontendUrl) {

    /** provider: "brevo" (API HTTP, recomendado en PaaS que bloquean SMTP) o "smtp". */
    public record Email(boolean enabled, String provider, String from, String fromName, String brevoApiKey,
                        String brevoApiUrl) {

        public boolean usesBrevo() {
            return "brevo".equalsIgnoreCase(provider);
        }
    }

    public record WhatsApp(boolean enabled, String accountSid, String authToken, String from, String apiBaseUrl) {

        public boolean configured() {
            return enabled && notBlank(accountSid) && notBlank(authToken) && notBlank(from);
        }
    }

    public record Retry(int maxAttempts, long backoffMillis) {
    }

    static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}

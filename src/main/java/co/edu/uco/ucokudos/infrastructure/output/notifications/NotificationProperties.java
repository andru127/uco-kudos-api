package co.edu.uco.ucokudos.infrastructure.output.notifications;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kudos.notifications")
public record NotificationProperties(Email email, WhatsApp whatsapp, Retry retry, String frontendUrl) {

    public record Email(boolean enabled, String provider, String from, String fromName, String brevoApiKey,
                        String brevoApiUrl) {

        public boolean usesBrevo() {
            return "brevo".equalsIgnoreCase(provider);
        }
    }

    public record WhatsApp(boolean enabled, String accountSid, String authToken, String from, String apiBaseUrl,
                           String contentSid, String contentVariables) {

        public boolean usesTemplate() {
            return notBlank(contentSid);
        }

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

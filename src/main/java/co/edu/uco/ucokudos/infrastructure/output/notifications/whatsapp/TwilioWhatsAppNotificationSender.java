package co.edu.uco.ucokudos.infrastructure.output.notifications.whatsapp;

import co.edu.uco.ucokudos.domain.notification.KudoNotification;
import co.edu.uco.ucokudos.domain.notification.NotificationChannel;
import co.edu.uco.ucokudos.domain.notification.NotificationResult;
import co.edu.uco.ucokudos.infrastructure.output.notifications.NotificationProperties;
import co.edu.uco.ucokudos.services.output.ports.notifications.NotificationSender;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import co.edu.uco.ucokudos.infrastructure.output.notifications.HttpClients;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Canal WhatsApp mediante la API REST de Twilio (sandbox en la línea base).
 * Patrón Adapter: traduce nuestro puerto NotificationSender al contrato HTTP de Twilio.
 * Se usa RestClient directamente (sin SDK) para mantener la imagen pequeña y con menos dependencias vulnerables.
 */
@Component
public class TwilioWhatsAppNotificationSender implements NotificationSender {

    private final NotificationProperties.WhatsApp config;
    private final RestClient restClient;

    public TwilioWhatsAppNotificationSender(NotificationProperties properties) {
        this.config = properties.whatsapp();
        this.restClient = RestClient.builder().requestFactory(HttpClients.withTimeouts()).baseUrl(config.apiBaseUrl()).build();
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.WHATSAPP;
    }

    @Override
    public NotificationResult send(KudoNotification notification) {
        if (!config.configured()) {
            return NotificationResult.skipped(channel(), "Canal WhatsApp no configurado");
        }
        var form = new LinkedMultiValueMap<String, String>();
        form.add("From", "whatsapp:" + config.from());
        form.add("To", "whatsapp:" + notification.recipientPhone());
        form.add("Body", "🏅 " + notification.title() + "\n" + notification.body() + "\n\nUCO Kudos");
        try {
            restClient.post()
                    .uri("/2010-04-01/Accounts/{sid}/Messages.json", config.accountSid())
                    .header(HttpHeaders.AUTHORIZATION, basicAuth())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
            return NotificationResult.sent(channel(), "WhatsApp enviado a " + mask(notification.recipientPhone()));
        } catch (RestClientException e) {
            return NotificationResult.failed(channel(), e.getMessage());
        }
    }

    private String basicAuth() {
        String credentials = config.accountSid() + ":" + config.authToken();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    /** No se escribe el número completo en auditoría ni logs (minimización de datos personales). */
    static String mask(String phone) {
        return phone.length() <= 4 ? "****" : "*".repeat(phone.length() - 4) + phone.substring(phone.length() - 4);
    }
}

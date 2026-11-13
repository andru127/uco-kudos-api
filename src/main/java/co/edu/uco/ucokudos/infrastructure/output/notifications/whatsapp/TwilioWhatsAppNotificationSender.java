package co.edu.uco.ucokudos.infrastructure.output.notifications.whatsapp;

import co.edu.uco.ucokudos.domain.notification.KudoNotification;
import co.edu.uco.ucokudos.domain.notification.NotificationChannel;
import co.edu.uco.ucokudos.domain.notification.NotificationResult;
import co.edu.uco.ucokudos.infrastructure.output.notifications.HttpClients;
import co.edu.uco.ucokudos.infrastructure.output.notifications.NotificationProperties;
import co.edu.uco.ucokudos.services.output.ports.notifications.NotificationSender;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class TwilioWhatsAppNotificationSender implements NotificationSender {

    private final NotificationProperties.WhatsApp config;
    private final RestClient restClient;

    public TwilioWhatsAppNotificationSender(NotificationProperties properties) {
        this.config = properties.whatsapp();
        this.restClient = RestClient.builder().requestFactory(HttpClients.withTimeouts())
                .baseUrl(config.apiBaseUrl()).build();
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
        try {
            restClient.post()
                    .uri("/2010-04-01/Accounts/{sid}/Messages.json", config.accountSid())
                    .header(HttpHeaders.AUTHORIZATION, basicAuth())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(buildForm(notification))
                    .retrieve()
                    .toBodilessEntity();
            return NotificationResult.sent(channel(), "WhatsApp enviado a " + mask(notification.recipientPhone()));
        } catch (RestClientException e) {
            return NotificationResult.failed(channel(), e.getMessage());
        }
    }

    private MultiValueMap<String, String> buildForm(KudoNotification notification) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("From", "whatsapp:" + config.from());
        form.add("To", "whatsapp:" + notification.recipientPhone());
        if (config.usesTemplate()) {
            form.add("ContentSid", config.contentSid());
            if (config.contentVariables() != null && !config.contentVariables().isBlank()) {
                form.add("ContentVariables", fillVariables(config.contentVariables(), notification));
            }
        } else {
            form.add("Body", "🏅 " + notification.title() + "\n" + notification.body() + "\n\nUCO Kudos");
        }
        return form;
    }

    static String fillVariables(String template, KudoNotification notification) {
        return template
                .replace("{author}", jsonEscape(notification.authorName()))
                .replace("{category}", jsonEscape(notification.categoryName()))
                .replace("{recipient}", jsonEscape(notification.recipientName()))
                .replace("{preview}", jsonEscape(notification.contentPreview()));
    }

    private static String jsonEscape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", " ");
    }

    private String basicAuth() {
        String credentials = config.accountSid() + ":" + config.authToken();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    static String mask(String phone) {
        return phone.length() <= 4 ? "****" : "*".repeat(phone.length() - 4) + phone.substring(phone.length() - 4);
    }
}

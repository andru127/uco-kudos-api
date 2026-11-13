package co.edu.uco.ucokudos.infrastructure.output.notifications.email;

import co.edu.uco.ucokudos.domain.notification.KudoNotification;
import co.edu.uco.ucokudos.domain.notification.NotificationChannel;
import co.edu.uco.ucokudos.domain.notification.NotificationResult;
import co.edu.uco.ucokudos.infrastructure.output.notifications.NotificationProperties;
import co.edu.uco.ucokudos.services.output.ports.notifications.NotificationSender;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import co.edu.uco.ucokudos.infrastructure.output.notifications.HttpClients;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Canal de correo con dos proveedores intercambiables por configuración:
 * <ul>
 *   <li>brevo: API HTTP transaccional de Brevo (funciona en PaaS que bloquean puertos SMTP, como Render free).</li>
 *   <li>smtp: cualquier servidor SMTP (Gmail con contraseña de aplicación, Outlook, Mailtrap...).</li>
 * </ul>
 */
@Component
public class EmailNotificationSender implements NotificationSender {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final NotificationProperties properties;
    private final RestClient brevoClient;

    public EmailNotificationSender(ObjectProvider<JavaMailSender> mailSenderProvider,
                                   NotificationProperties properties) {
        this.mailSenderProvider = mailSenderProvider;
        this.properties = properties;
        this.brevoClient = RestClient.builder().requestFactory(HttpClients.withTimeouts()).baseUrl(properties.email().brevoApiUrl()).build();
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public NotificationResult send(KudoNotification notification) {
        var config = properties.email();
        if (!config.enabled()) {
            return NotificationResult.skipped(channel(), "Canal de correo no configurado");
        }
        return config.usesBrevo() ? sendWithBrevo(notification) : sendWithSmtp(notification);
    }

    private NotificationResult sendWithBrevo(KudoNotification notification) {
        var config = properties.email();
        if (config.brevoApiKey() == null || config.brevoApiKey().isBlank()) {
            return NotificationResult.skipped(channel(), "Falta BREVO_API_KEY");
        }
        var body = Map.of(
                "sender", Map.of("email", config.from(), "name", config.fromName()),
                "to", List.of(Map.of("email", notification.recipientEmail(), "name", notification.recipientName())),
                "subject", notification.title(),
                "htmlContent", buildHtml(notification));
        try {
            brevoClient.post()
                    .uri("/v3/smtp/email")
                    .header("api-key", config.brevoApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            return NotificationResult.sent(channel(), "Correo (Brevo) enviado a " + notification.recipientEmail());
        } catch (RestClientException e) {
            return NotificationResult.failed(channel(), e.getMessage());
        }
    }

    private NotificationResult sendWithSmtp(KudoNotification notification) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            return NotificationResult.skipped(channel(), "SMTP no configurado");
        }
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.email().from(), properties.email().fromName());
            helper.setTo(notification.recipientEmail());
            helper.setSubject(notification.title());
            helper.setText(buildHtml(notification), true);
            mailSender.send(message);
            return NotificationResult.sent(channel(), "Correo (SMTP) enviado a " + notification.recipientEmail());
        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException e) {
            return NotificationResult.failed(channel(), e.getMessage());
        }
    }

    /** Todo dato de usuario se escapa: evita inyección de HTML en el correo. */
    private String buildHtml(KudoNotification notification) {
        String link = properties.frontendUrl() + "/?kudo=" + notification.kudoId();
        return """
                <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto">
                  <h2 style="color:#00563f">¡Hola %s!</h2>
                  <p><strong>%s</strong> te reconoció en la categoría <strong>%s</strong>.</p>
                  <blockquote style="border-left:4px solid #c9a227;padding-left:12px;color:#333">%s</blockquote>
                  <p><a href="%s" style="background:#00563f;color:#fff;padding:10px 16px;border-radius:6px;text-decoration:none">Ver mi Kudo</a></p>
                  <p style="color:#888;font-size:12px">UCO Kudos · Universidad Católica de Oriente</p>
                </div>
                """.formatted(
                HtmlUtils.htmlEscape(notification.recipientName()),
                HtmlUtils.htmlEscape(notification.authorName()),
                HtmlUtils.htmlEscape(notification.categoryName()),
                HtmlUtils.htmlEscape(notification.contentPreview()),
                HtmlUtils.htmlEscape(link));
    }
}

package co.edu.uco.ucokudos.infrastructure.output.notifications.email;

import co.edu.uco.ucokudos.domain.notification.KudoNotification;
import co.edu.uco.ucokudos.domain.notification.NotificationStatus;
import co.edu.uco.ucokudos.infrastructure.output.notifications.NotificationProperties;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailNotificationSenderTest {

    private final KudoNotification notification = new KudoNotification(UUID.randomUUID(), UUID.randomUUID(),
            "Ana <b>", "ana@uco.edu.co", "+573001234567", "Carlos", "Liderazgo", "¡Gracias!");

    @Test
    void omiteElEnvioSiElCanalEstaDeshabilitado() {
        var sender = new EmailNotificationSender(provider(null), properties(false, "brevo", "clave"));

        assertThat(sender.send(notification).status()).isEqualTo(NotificationStatus.SKIPPED);
    }

    @Test
    void omiteElEnvioConBrevoSinApiKey() {
        var sender = new EmailNotificationSender(provider(null), properties(true, "brevo", ""));

        assertThat(sender.send(notification).status()).isEqualTo(NotificationStatus.SKIPPED);
    }

    @Test
    void reportaFalloSiBrevoNoResponde() {
        var sender = new EmailNotificationSender(provider(null), properties(true, "brevo", "clave"));

        assertThat(sender.send(notification).status()).isEqualTo(NotificationStatus.FAILED);
    }

    @Test
    void omiteElEnvioSmtpSinServidorConfigurado() {
        var sender = new EmailNotificationSender(provider(null), properties(true, "smtp", null));

        assertThat(sender.send(notification).status()).isEqualTo(NotificationStatus.SKIPPED);
    }

    @Test
    void enviaPorSmtpCuandoHayServidor() {
        var mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        var sender = new EmailNotificationSender(provider(mailSender), properties(true, "smtp", null));

        var result = sender.send(notification);

        assertThat(result.status()).isEqualTo(NotificationStatus.SENT);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void reportaFalloSiElServidorSmtpRechazaElCorreo() {
        var mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        doThrow(new MailSendException("SMTP caído")).when(mailSender).send(any(MimeMessage.class));
        var sender = new EmailNotificationSender(provider(mailSender), properties(true, "smtp", null));

        assertThat(sender.send(notification).status()).isEqualTo(NotificationStatus.FAILED);
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<JavaMailSender> provider(JavaMailSender mailSender) {
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(mailSender);
        return provider;
    }

    private static NotificationProperties properties(boolean enabled, String provider, String apiKey) {
        return new NotificationProperties(
                new NotificationProperties.Email(enabled, provider, "no-reply@uco.edu.co", "UCO Kudos", apiKey,
                        "http://localhost:1"),
                new NotificationProperties.WhatsApp(false, null, null, null, "http://localhost:1", null, null),
                new NotificationProperties.Retry(1, 0),
                "http://localhost:5173");
    }
}

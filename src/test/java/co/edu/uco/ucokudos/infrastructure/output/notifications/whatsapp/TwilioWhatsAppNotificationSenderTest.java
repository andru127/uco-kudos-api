package co.edu.uco.ucokudos.infrastructure.output.notifications.whatsapp;

import co.edu.uco.ucokudos.domain.notification.KudoNotification;
import co.edu.uco.ucokudos.domain.notification.NotificationStatus;
import co.edu.uco.ucokudos.infrastructure.output.notifications.NotificationProperties;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TwilioWhatsAppNotificationSenderTest {

    private final KudoNotification notification = new KudoNotification(UUID.randomUUID(), UUID.randomUUID(),
            "Ana", "ana@uco.edu.co", "+573001234567", "Carlos \"Profe\"", "Liderazgo", "¡Gracias!");

    @Test
    void llenaLasVariablesDeLaPlantillaEscapandoComillas() {
        String json = TwilioWhatsAppNotificationSender.fillVariables("{\"1\":\"{author}\",\"2\":\"{category}\"}",
                notification);

        assertThat(json).isEqualTo("{\"1\":\"Carlos \\\"Profe\\\"\",\"2\":\"Liderazgo\"}");
    }

    @Test
    void enmascaraElCelular() {
        assertThat(TwilioWhatsAppNotificationSender.mask("+573001234567")).isEqualTo("*********4567");
        assertThat(TwilioWhatsAppNotificationSender.mask("123")).isEqualTo("****");
    }

    @Test
    void omiteElEnvioSiNoEstaConfigurado() {
        var sender = new TwilioWhatsAppNotificationSender(properties(false, null, null));

        assertThat(sender.send(notification).status()).isEqualTo(NotificationStatus.SKIPPED);
    }

    @Test
    void reportaFalloConMensajeLibreSiTwilioNoResponde() {
        var sender = new TwilioWhatsAppNotificationSender(properties(true, null, null));

        assertThat(sender.send(notification).status()).isEqualTo(NotificationStatus.FAILED);
    }

    @Test
    void reportaFalloConPlantillaSiTwilioNoResponde() {
        var sender = new TwilioWhatsAppNotificationSender(
                properties(true, "HX123", "{\"1\":\"{author}\",\"2\":\"{preview}\"}"));

        assertThat(sender.send(notification).status()).isEqualTo(NotificationStatus.FAILED);
    }

    private static NotificationProperties properties(boolean enabled, String contentSid, String variables) {
        return new NotificationProperties(
                new NotificationProperties.Email(false, "brevo", "no-reply@uco.edu.co", "UCO Kudos", null,
                        "http://localhost:1"),
                new NotificationProperties.WhatsApp(enabled, "AC123", "token", "+14155238886", "http://localhost:1",
                        contentSid, variables),
                new NotificationProperties.Retry(1, 0),
                "http://localhost:5173");
    }
}

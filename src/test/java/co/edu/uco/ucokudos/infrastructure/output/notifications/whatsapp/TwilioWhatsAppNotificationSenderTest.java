package co.edu.uco.ucokudos.infrastructure.output.notifications.whatsapp;

import co.edu.uco.ucokudos.domain.notification.KudoNotification;
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
    }
}

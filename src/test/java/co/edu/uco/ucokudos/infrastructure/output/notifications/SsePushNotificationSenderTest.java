package co.edu.uco.ucokudos.infrastructure.output.notifications;

import co.edu.uco.ucokudos.domain.notification.KudoNotification;
import co.edu.uco.ucokudos.domain.notification.NotificationStatus;
import co.edu.uco.ucokudos.infrastructure.output.notifications.push.SseConnectionRegistry;
import co.edu.uco.ucokudos.infrastructure.output.notifications.push.SsePushNotificationSender;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SsePushNotificationSenderTest {

    private final SseConnectionRegistry registry = new SseConnectionRegistry();
    private final SsePushNotificationSender sender = new SsePushNotificationSender(registry);

    @Test
    void entregaSoloSiElDestinatarioEstaConectado() {
        var recipientId = UUID.randomUUID();
        registry.connect(recipientId);

        var result = sender.send(notificationFor(recipientId));

        assertThat(result.status()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void omiteSiElDestinatarioNoEstaConectado() {
        var result = sender.send(notificationFor(UUID.randomUUID()));

        assertThat(result.status()).isEqualTo(NotificationStatus.SKIPPED);
    }

    private static KudoNotification notificationFor(UUID recipientId) {
        return new KudoNotification(UUID.randomUUID(), recipientId, "Ana", "ana@uco.edu.co", "+573001234567",
                "Carlos", "Liderazgo", "¡Gracias!");
    }
}

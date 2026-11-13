package co.edu.uco.ucokudos.infrastructure.output.notifications.push;

import co.edu.uco.ucokudos.domain.notification.KudoNotification;
import co.edu.uco.ucokudos.domain.notification.NotificationChannel;
import co.edu.uco.ucokudos.domain.notification.NotificationResult;
import co.edu.uco.ucokudos.services.output.ports.notifications.NotificationSender;
import org.springframework.stereotype.Component;

/** Notificación PUSH en tiempo real, solo si el destinatario tiene la aplicación abierta. */
@Component
public class SsePushNotificationSender implements NotificationSender {

    public static final String EVENT_NAME = "kudo";

    private final SseConnectionRegistry registry;

    public SsePushNotificationSender(SseConnectionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public NotificationResult send(KudoNotification notification) {
        if (!registry.isConnected(notification.recipientId())) {
            return NotificationResult.skipped(channel(), "Destinatario no conectado");
        }
        int delivered = registry.send(notification.recipientId(), EVENT_NAME,
                new PushPayload(notification.kudoId(), notification.title(), notification.body()));
        return delivered > 0
                ? NotificationResult.sent(channel(), "Entregado en " + delivered + " sesión(es) activa(s)")
                : NotificationResult.failed(channel(), "Las conexiones activas se cerraron antes del envío");
    }
}

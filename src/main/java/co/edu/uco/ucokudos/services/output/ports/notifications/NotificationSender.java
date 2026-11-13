package co.edu.uco.ucokudos.services.output.ports.notifications;

import co.edu.uco.ucokudos.domain.notification.KudoNotification;
import co.edu.uco.ucokudos.domain.notification.NotificationChannel;
import co.edu.uco.ucokudos.domain.notification.NotificationResult;

public interface NotificationSender {

    NotificationChannel channel();

    NotificationResult send(KudoNotification notification);
}

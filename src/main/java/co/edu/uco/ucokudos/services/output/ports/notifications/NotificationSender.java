package co.edu.uco.ucokudos.services.output.ports.notifications;

import co.edu.uco.ucokudos.domain.notification.KudoNotification;
import co.edu.uco.ucokudos.domain.notification.NotificationChannel;
import co.edu.uco.ucokudos.domain.notification.NotificationResult;

/**
 * Puerto de salida para un canal de notificación (patrón Strategy).
 * Agregar un canal nuevo (SMS, Teams) = una nueva implementación, sin tocar los casos de uso (OCP).
 * Las implementaciones nunca lanzan excepción: devuelven el resultado para que quede auditado.
 */
public interface NotificationSender {

    NotificationChannel channel();

    NotificationResult send(KudoNotification notification);
}

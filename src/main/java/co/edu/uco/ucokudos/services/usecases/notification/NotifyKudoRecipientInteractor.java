package co.edu.uco.ucokudos.services.usecases.notification;

import co.edu.uco.ucokudos.domain.audit.AuditEvent;
import co.edu.uco.ucokudos.domain.notification.NotificationResult;
import co.edu.uco.ucokudos.services.output.ports.events.KudoPublishedEvent;
import co.edu.uco.ucokudos.services.output.ports.notifications.NotificationSender;
import co.edu.uco.ucokudos.services.output.ports.repositories.AuditRepository;
import co.edu.uco.ucokudos.services.usecases.UseCaseWithoutReturn;

import java.time.Clock;
import java.util.List;

/**
 * Envía la notificación por TODOS los canales registrados (correo, WhatsApp, push) y audita el
 * resultado de cada uno. Un canal que falla no impide que los demás se envíen (aislamiento de fallos).
 */
public class NotifyKudoRecipientInteractor implements UseCaseWithoutReturn<KudoPublishedEvent> {

    private final List<NotificationSender> senders;
    private final AuditRepository auditRepository;
    private final Clock clock;

    public NotifyKudoRecipientInteractor(List<NotificationSender> senders, AuditRepository auditRepository,
                                         Clock clock) {
        this.senders = List.copyOf(senders);
        this.auditRepository = auditRepository;
        this.clock = clock;
    }

    @Override
    public void execute(KudoPublishedEvent event) {
        for (NotificationSender sender : senders) {
            NotificationResult result = sender.send(event.notification());
            auditRepository.record(AuditEvent.ofNotification(event.notification().kudoId(), result, clock.instant()));
        }
    }
}

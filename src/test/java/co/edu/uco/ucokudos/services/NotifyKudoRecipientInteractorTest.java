package co.edu.uco.ucokudos.services;

import co.edu.uco.ucokudos.domain.audit.AuditAction;
import co.edu.uco.ucokudos.domain.audit.AuditEvent;
import co.edu.uco.ucokudos.domain.notification.KudoNotification;
import co.edu.uco.ucokudos.domain.notification.NotificationChannel;
import co.edu.uco.ucokudos.domain.notification.NotificationResult;
import co.edu.uco.ucokudos.services.output.ports.events.KudoPublishedEvent;
import co.edu.uco.ucokudos.services.output.ports.notifications.NotificationSender;
import co.edu.uco.ucokudos.services.output.ports.repositories.AuditRepository;
import co.edu.uco.ucokudos.services.usecases.notification.NotifyKudoRecipientInteractor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class NotifyKudoRecipientInteractorTest {

    private final AuditRepository audit = mock(AuditRepository.class);

    @Test
    void enviaPorTodosLosCanalesYAuditaCadaResultadoAunqueUnoFalle() {
        var email = sender(NotificationChannel.EMAIL, n -> NotificationResult.sent(NotificationChannel.EMAIL, "ok"));
        var whatsapp = sender(NotificationChannel.WHATSAPP,
                n -> NotificationResult.failed(NotificationChannel.WHATSAPP, "Twilio caído"));
        var push = sender(NotificationChannel.PUSH,
                n -> NotificationResult.skipped(NotificationChannel.PUSH, "no conectado"));
        var interactor = new NotifyKudoRecipientInteractor(List.of(email, whatsapp, push), audit, Clock.systemUTC());

        interactor.execute(new KudoPublishedEvent(notification()));

        var captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(audit, times(3)).record(captor.capture());
        assertThat(captor.getAllValues()).extracting(AuditEvent::action).containsExactly(
                AuditAction.NOTIFICATION_SENT, AuditAction.NOTIFICATION_FAILED, AuditAction.NOTIFICATION_SKIPPED);
    }

    static KudoNotification notification() {
        return new KudoNotification(UUID.randomUUID(), UUID.randomUUID(), "Ana", "ana@uco.edu.co", "+573001234567",
                "Carlos", "Liderazgo", "¡Gracias!");
    }

    private static NotificationSender sender(NotificationChannel channel,
                                             Function<KudoNotification, NotificationResult> behavior) {
        return new NotificationSender() {
            @Override
            public NotificationChannel channel() {
                return channel;
            }

            @Override
            public NotificationResult send(KudoNotification notification) {
                return behavior.apply(notification);
            }
        };
    }
}

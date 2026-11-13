package co.edu.uco.ucokudos.infrastructure.input.events;

import co.edu.uco.ucokudos.infrastructure.config.async.AsyncConfig;
import co.edu.uco.ucokudos.services.output.ports.events.KudoPublishedEvent;
import co.edu.uco.ucokudos.services.usecases.notification.NotifyKudoRecipientInteractor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Consumidor del evento KudoPublished (orientación a mensajes del Manifiesto Reactivo).
 * AFTER_COMMIT: solo se notifica si el Kudo realmente quedó guardado.
 * @Async: el usuario que publica recibe su respuesta sin esperar a SMTP ni a Twilio.
 */
@Component
public class KudoPublishedListener {

    private final NotifyKudoRecipientInteractor notifyKudoRecipient;

    public KudoPublishedListener(NotifyKudoRecipientInteractor notifyKudoRecipient) {
        this.notifyKudoRecipient = notifyKudoRecipient;
    }

    @Async(AsyncConfig.NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onKudoPublished(KudoPublishedEvent event) {
        notifyKudoRecipient.execute(event);
    }
}

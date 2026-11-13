package co.edu.uco.ucokudos.infrastructure.output.notifications;

import co.edu.uco.ucokudos.domain.notification.KudoNotification;
import co.edu.uco.ucokudos.domain.notification.NotificationChannel;
import co.edu.uco.ucokudos.domain.notification.NotificationResult;
import co.edu.uco.ucokudos.domain.notification.NotificationStatus;
import co.edu.uco.ucokudos.services.output.ports.notifications.NotificationSender;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorador (patrón Decorator) que agrega a cualquier canal:
 * <ul>
 *   <li>Reintentos con backoff lineal ante fallos transitorios (resiliencia).</li>
 *   <li>Métricas: contador kudos.notifications{channel,status} y temporizador por canal (observabilidad).</li>
 *   <li>Contención: ninguna excepción escapa; se convierte en resultado FAILED.</li>
 * </ul>
 */
public class ResilientNotificationSender implements NotificationSender {

    private static final Logger LOG = LoggerFactory.getLogger(ResilientNotificationSender.class);

    private final NotificationSender delegate;
    private final MeterRegistry meterRegistry;
    private final int maxAttempts;
    private final long backoffMillis;

    public ResilientNotificationSender(NotificationSender delegate, MeterRegistry meterRegistry,
                                       int maxAttempts, long backoffMillis) {
        this.delegate = delegate;
        this.meterRegistry = meterRegistry;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.backoffMillis = Math.max(0, backoffMillis);
    }

    @Override
    public NotificationChannel channel() {
        return delegate.channel();
    }

    @Override
    public NotificationResult send(KudoNotification notification) {
        Timer.Sample sample = Timer.start(meterRegistry);
        NotificationResult result = attemptWithRetries(notification);
        sample.stop(meterRegistry.timer("kudos.notifications.duration", "channel", channel().name()));
        meterRegistry.counter("kudos.notifications", "channel", channel().name(), "status", result.status().name())
                .increment();
        LOG.info("Notificación {} para kudo {}: {} ({})", channel(), notification.kudoId(), result.status(),
                result.detail());
        return result;
    }

    private NotificationResult attemptWithRetries(KudoNotification notification) {
        NotificationResult last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            last = safeSend(notification);
            if (last.status() != NotificationStatus.FAILED) {
                return last;
            }
            LOG.warn("Intento {}/{} fallido en canal {}: {}", attempt, maxAttempts, channel(), last.detail());
            if (attempt < maxAttempts && !pause(backoffMillis * attempt)) {
                break;
            }
        }
        return NotificationResult.failed(channel(), last.detail() + " (tras " + maxAttempts + " intentos)");
    }

    private NotificationResult safeSend(KudoNotification notification) {
        try {
            return delegate.send(notification);
        } catch (RuntimeException e) {
            return NotificationResult.failed(channel(), e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static boolean pause(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}

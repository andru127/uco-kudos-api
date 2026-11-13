package co.edu.uco.ucokudos.infrastructure.output.notifications;

import co.edu.uco.ucokudos.domain.notification.KudoNotification;
import co.edu.uco.ucokudos.domain.notification.NotificationChannel;
import co.edu.uco.ucokudos.domain.notification.NotificationResult;
import co.edu.uco.ucokudos.domain.notification.NotificationStatus;
import co.edu.uco.ucokudos.services.output.ports.notifications.NotificationSender;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ResilientNotificationSenderTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final KudoNotification notification = new KudoNotification(UUID.randomUUID(), UUID.randomUUID(), "Ana",
            "ana@uco.edu.co", "+573001234567", "Carlos", "Liderazgo", "¡Gracias!");

    @Test
    void reintentaHastaTenerExitoYRegistraLaMetrica() {
        var calls = new AtomicInteger();
        var flaky = sender(() -> calls.incrementAndGet() < 3
                ? NotificationResult.failed(NotificationChannel.WHATSAPP, "timeout")
                : NotificationResult.sent(NotificationChannel.WHATSAPP, "ok"));

        var result = new ResilientNotificationSender(flaky, registry, 3, 0).send(notification);

        assertThat(result.status()).isEqualTo(NotificationStatus.SENT);
        assertThat(calls).hasValue(3);
        assertThat(registry.counter("kudos.notifications", "channel", "WHATSAPP", "status", "SENT").count())
                .isEqualTo(1.0);
    }

    @Test
    void convierteExcepcionesEnFalloSinPropagarlas() {
        var broken = sender(() -> {
            throw new IllegalStateException("explotó");
        });

        var result = new ResilientNotificationSender(broken, registry, 2, 0).send(notification);

        assertThat(result.status()).isEqualTo(NotificationStatus.FAILED);
        assertThat(result.detail()).contains("explotó").contains("2 intentos");
    }

    @Test
    void noReintentaCuandoElCanalNoAplica() {
        var calls = new AtomicInteger();
        var skipped = sender(() -> {
            calls.incrementAndGet();
            return NotificationResult.skipped(NotificationChannel.WHATSAPP, "no configurado");
        });

        new ResilientNotificationSender(skipped, registry, 3, 0).send(notification);

        assertThat(calls).hasValue(1);
    }

    private static NotificationSender sender(java.util.function.Supplier<NotificationResult> behavior) {
        return new NotificationSender() {
            @Override
            public NotificationChannel channel() {
                return NotificationChannel.WHATSAPP;
            }

            @Override
            public NotificationResult send(KudoNotification notification) {
                return behavior.get();
            }
        };
    }
}

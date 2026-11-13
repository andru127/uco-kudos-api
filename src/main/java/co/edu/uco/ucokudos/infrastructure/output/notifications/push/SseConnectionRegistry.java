package co.edu.uco.ucokudos.infrastructure.output.notifications.push;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registro de conexiones Server-Sent Events abiertas por usuario (un usuario puede tener varias pestañas
 * o dispositivos). Es lo que permite saber si el destinatario "está conectado" (paso 4 del flujo).
 * Limitación conocida: vive en memoria de una instancia; para escalar horizontalmente se propone
 * Redis Pub/Sub (ver ADR-005).
 */
@Component
public class SseConnectionRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(SseConnectionRegistry.class);
    private static final long TIMEOUT_MILLIS = Duration.ofMinutes(30).toMillis();

    private final Map<UUID, List<SseEmitter>> connections = new ConcurrentHashMap<>();

    public SseEmitter connect(UUID userId) {
        var emitter = new SseEmitter(TIMEOUT_MILLIS);
        connections.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(emitter);
        Runnable cleanup = () -> remove(userId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(error -> cleanup.run());
        sendTo(emitter, userId, "connected", "ok");
        return emitter;
    }

    public boolean isConnected(UUID userId) {
        return !connections.getOrDefault(userId, List.of()).isEmpty();
    }

    public int activeConnections() {
        return connections.values().stream().mapToInt(List::size).sum();
    }

    /** @return número de sesiones del usuario que recibieron el evento. */
    public int send(UUID userId, String eventName, Object payload) {
        int delivered = 0;
        for (SseEmitter emitter : connections.getOrDefault(userId, List.of())) {
            if (sendTo(emitter, userId, eventName, payload)) {
                delivered++;
            }
        }
        return delivered;
    }

    /** Latido cada 25 s: evita que proxies (Render, Nginx) cierren conexiones inactivas. */
    @Scheduled(fixedRate = 25_000)
    public void heartbeat() {
        connections.forEach((userId, emitters) ->
                emitters.forEach(emitter -> sendTo(emitter, userId, "heartbeat", "ping")));
    }

    private boolean sendTo(SseEmitter emitter, UUID userId, String eventName, Object payload) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(payload, MediaType.APPLICATION_JSON));
            return true;
        } catch (IOException | IllegalStateException e) {
            LOG.debug("Conexión SSE cerrada para {}: {}", userId, e.getMessage());
            remove(userId, emitter);
            return false;
        }
    }

    private void remove(UUID userId, SseEmitter emitter) {
        connections.computeIfPresent(userId, (id, list) -> {
            list.remove(emitter);
            return list.isEmpty() ? null : list;
        });
    }
}

package co.edu.uco.ucokudos.infrastructure.output.notifications.push;

import java.util.UUID;

public record PushPayload(UUID kudoId, String title, String body) {
}

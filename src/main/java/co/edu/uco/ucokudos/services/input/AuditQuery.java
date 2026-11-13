package co.edu.uco.ucokudos.services.input;

import java.util.UUID;

public record AuditQuery(UUID actorId, UUID resourceId) {
}

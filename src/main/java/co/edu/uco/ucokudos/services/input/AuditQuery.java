package co.edu.uco.ucokudos.services.input;

import java.util.UUID;

/** resourceId es opcional: si es null se devuelven los eventos más recientes. */
public record AuditQuery(UUID actorId, UUID resourceId) {
}

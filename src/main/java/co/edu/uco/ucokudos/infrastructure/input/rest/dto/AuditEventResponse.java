package co.edu.uco.ucokudos.infrastructure.input.rest.dto;

import co.edu.uco.ucokudos.domain.audit.AuditEvent;

import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(UUID id, Instant occurredAt, UUID actorId, String action, String resourceType,
                                 UUID resourceId, String detail) {

    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(event.id(), event.occurredAt(), event.actorId(), event.action().name(),
                event.resourceType(), event.resourceId(), event.detail());
    }
}

package co.edu.uco.ucokudos.domain.audit;

import co.edu.uco.ucokudos.domain.notification.NotificationResult;

import java.time.Instant;
import java.util.UUID;

public record AuditEvent(UUID id, Instant occurredAt, UUID actorId, AuditAction action,
                         String resourceType, UUID resourceId, String detail) {

    public static AuditEvent of(UUID actorId, AuditAction action, String resourceType, UUID resourceId,
                                String detail, Instant now) {
        return new AuditEvent(UUID.randomUUID(), now, actorId, action, resourceType, resourceId, detail);
    }

    public static AuditEvent ofNotification(UUID kudoId, NotificationResult result, Instant now) {
        AuditAction action = switch (result.status()) {
            case SENT -> AuditAction.NOTIFICATION_SENT;
            case FAILED -> AuditAction.NOTIFICATION_FAILED;
            case SKIPPED -> AuditAction.NOTIFICATION_SKIPPED;
        };
        return of(null, action, "KUDO", kudoId, result.channel() + ": " + result.detail(), now);
    }
}

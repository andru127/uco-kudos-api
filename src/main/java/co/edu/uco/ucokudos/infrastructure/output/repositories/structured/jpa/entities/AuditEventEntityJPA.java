package co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class AuditEventEntityJPA {

    @Id
    private UUID id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(nullable = false)
    private String action;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    private String detail;

    @Column(name = "trace_id")
    private String traceId;

    protected AuditEventEntityJPA() {
        // Requerido por JPA
    }

    public AuditEventEntityJPA(UUID id, Instant occurredAt, UUID actorId, String action, String resourceType,
                               UUID resourceId, String detail, String traceId) {
        this.id = id;
        this.occurredAt = occurredAt;
        this.actorId = actorId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.detail = detail;
        this.traceId = traceId;
    }

    public UUID getId() { return id; }
    public Instant getOccurredAt() { return occurredAt; }
    public UUID getActorId() { return actorId; }
    public String getAction() { return action; }
    public String getResourceType() { return resourceType; }
    public UUID getResourceId() { return resourceId; }
    public String getDetail() { return detail; }
    public String getTraceId() { return traceId; }
}

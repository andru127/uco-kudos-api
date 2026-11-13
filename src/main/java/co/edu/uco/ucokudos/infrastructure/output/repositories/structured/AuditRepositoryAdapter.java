package co.edu.uco.ucokudos.infrastructure.output.repositories.structured;

import co.edu.uco.ucokudos.domain.audit.AuditAction;
import co.edu.uco.ucokudos.domain.audit.AuditEvent;
import co.edu.uco.ucokudos.infrastructure.config.observability.TraceIdFilter;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.AuditEventJPARepository;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities.AuditEventEntityJPA;
import co.edu.uco.ucokudos.services.output.ports.repositories.AuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Persiste la auditoría y además la emite como log estructurado (doble rastro: BD + logs).
 * Cada registro guarda el traceId de la petición para correlacionarlo con los logs.
 */
@Component
public class AuditRepositoryAdapter implements AuditRepository {

    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");
    private static final int MAX_DETAIL = 1000;

    private final AuditEventJPARepository repository;

    public AuditRepositoryAdapter(AuditEventJPARepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void record(AuditEvent event) {
        String detail = event.detail() == null || event.detail().length() <= MAX_DETAIL
                ? event.detail() : event.detail().substring(0, MAX_DETAIL);
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);
        repository.save(new AuditEventEntityJPA(event.id(), event.occurredAt(), event.actorId(),
                event.action().name(), event.resourceType(), event.resourceId(), detail, traceId));
        AUDIT_LOG.info("action={} resourceType={} resourceId={} actorId={} detail=\"{}\"",
                event.action(), event.resourceType(), event.resourceId(), event.actorId(), detail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEvent> findRecent(UUID resourceId, int limit) {
        var page = PageRequest.of(0, limit);
        var entities = resourceId == null
                ? repository.findAllByOrderByOccurredAtDesc(page)
                : repository.findByResourceIdOrderByOccurredAtDesc(resourceId, page);
        return entities.stream()
                .map(entity -> new AuditEvent(entity.getId(), entity.getOccurredAt(), entity.getActorId(),
                        AuditAction.valueOf(entity.getAction()), entity.getResourceType(), entity.getResourceId(),
                        entity.getDetail()))
                .toList();
    }
}

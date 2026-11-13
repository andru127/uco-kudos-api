package co.edu.uco.ucokudos.services.output.ports.repositories;

import co.edu.uco.ucokudos.domain.audit.AuditEvent;

import java.util.List;
import java.util.UUID;

public interface AuditRepository {

    void record(AuditEvent event);

    List<AuditEvent> findRecent(UUID resourceId, int limit);
}

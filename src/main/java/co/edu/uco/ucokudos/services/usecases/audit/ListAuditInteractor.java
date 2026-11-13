package co.edu.uco.ucokudos.services.usecases.audit;

import co.edu.uco.ucokudos.domain.audit.AuditEvent;
import co.edu.uco.ucokudos.domain.exception.AccessDeniedDomainException;
import co.edu.uco.ucokudos.domain.exception.ResourceNotFoundException;
import co.edu.uco.ucokudos.services.input.AuditQuery;
import co.edu.uco.ucokudos.services.output.ports.repositories.AuditRepository;
import co.edu.uco.ucokudos.services.output.ports.repositories.UserRepository;
import co.edu.uco.ucokudos.services.usecases.UseCaseWithReturn;

import java.util.List;

public class ListAuditInteractor implements UseCaseWithReturn<AuditQuery, List<AuditEvent>> {

    private static final int LIMIT = 100;

    private final UserRepository userRepository;
    private final AuditRepository auditRepository;

    public ListAuditInteractor(UserRepository userRepository, AuditRepository auditRepository) {
        this.userRepository = userRepository;
        this.auditRepository = auditRepository;
    }

    @Override
    public List<AuditEvent> execute(AuditQuery query) {
        var actor = userRepository.findById(query.actorId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", query.actorId()));
        if (!actor.role().canReadAudit()) {
            throw new AccessDeniedDomainException("Tu rol no puede consultar la auditoría");
        }
        return auditRepository.findRecent(query.resourceId(), LIMIT);
    }
}

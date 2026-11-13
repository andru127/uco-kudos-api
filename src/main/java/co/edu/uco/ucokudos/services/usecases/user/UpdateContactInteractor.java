package co.edu.uco.ucokudos.services.usecases.user;

import co.edu.uco.ucokudos.domain.audit.AuditAction;
import co.edu.uco.ucokudos.domain.audit.AuditEvent;
import co.edu.uco.ucokudos.domain.exception.ResourceNotFoundException;
import co.edu.uco.ucokudos.domain.user.UserDomain;
import co.edu.uco.ucokudos.services.input.UpdateContactCommand;
import co.edu.uco.ucokudos.services.output.ports.repositories.AuditRepository;
import co.edu.uco.ucokudos.services.output.ports.repositories.UserRepository;
import co.edu.uco.ucokudos.services.usecases.UseCaseWithReturn;

import java.time.Clock;
import java.util.Locale;

public class UpdateContactInteractor implements UseCaseWithReturn<UpdateContactCommand, UserDomain> {

    private final UserRepository userRepository;
    private final AuditRepository auditRepository;
    private final Clock clock;

    public UpdateContactInteractor(UserRepository userRepository, AuditRepository auditRepository, Clock clock) {
        this.userRepository = userRepository;
        this.auditRepository = auditRepository;
        this.clock = clock;
    }

    @Override
    public UserDomain execute(UpdateContactCommand command) {
        var current = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", command.userId()));
        var updated = current.withContact(command.email().trim().toLowerCase(Locale.ROOT), command.phone().trim());
        userRepository.updateContact(updated);
        auditRepository.record(AuditEvent.of(updated.id(), AuditAction.CONTACT_UPDATED, "USER", updated.id(),
                "Correo y celular actualizados", clock.instant()));
        return updated;
    }
}

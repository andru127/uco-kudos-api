package co.edu.uco.ucokudos.services.usecases.auth;

import co.edu.uco.ucokudos.domain.audit.AuditAction;
import co.edu.uco.ucokudos.domain.audit.AuditEvent;
import co.edu.uco.ucokudos.domain.exception.InvalidCredentialsException;
import co.edu.uco.ucokudos.services.input.LoginCommand;
import co.edu.uco.ucokudos.services.input.LoginResult;
import co.edu.uco.ucokudos.services.output.ports.repositories.AuditRepository;
import co.edu.uco.ucokudos.services.output.ports.repositories.UserRepository;
import co.edu.uco.ucokudos.services.output.ports.security.PasswordHasher;
import co.edu.uco.ucokudos.services.output.ports.security.TokenIssuer;
import co.edu.uco.ucokudos.services.usecases.UseCaseWithReturn;

import java.time.Clock;
import java.util.Locale;

/** Autentica con correo institucional y contraseña y emite un JWT de vida corta. */
public class LoginInteractor implements UseCaseWithReturn<LoginCommand, LoginResult> {

    /** Mensaje genérico: no revela si el correo existe (evita enumeración de usuarios). */
    private static final String INVALID_CREDENTIALS = "Credenciales inválidas";

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final AuditRepository auditRepository;
    private final Clock clock;

    public LoginInteractor(UserRepository userRepository, PasswordHasher passwordHasher, TokenIssuer tokenIssuer,
                           AuditRepository auditRepository, Clock clock) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.auditRepository = auditRepository;
        this.clock = clock;
    }

    @Override
    public LoginResult execute(LoginCommand command) {
        String email = command.email() == null ? "" : command.email().trim().toLowerCase(Locale.ROOT);
        var credentials = userRepository.findCredentialsByEmail(email)
                .filter(found -> found.user().active())
                .filter(found -> passwordHasher.matches(command.password(), found.passwordHash()));

        if (credentials.isEmpty()) {
            auditRepository.record(AuditEvent.of(null, AuditAction.LOGIN_FAILED, "USER", null,
                    "Intento fallido para " + email, clock.instant()));
            throw new InvalidCredentialsException(INVALID_CREDENTIALS);
        }

        var user = credentials.get().user();
        auditRepository.record(AuditEvent.of(user.id(), AuditAction.LOGIN_SUCCEEDED, "USER", user.id(),
                "Inicio de sesión", clock.instant()));
        return new LoginResult(tokenIssuer.issue(user), tokenIssuer.expiresInSeconds(), user);
    }
}

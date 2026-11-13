package co.edu.uco.ucokudos.services;

import co.edu.uco.ucokudos.TestData;
import co.edu.uco.ucokudos.domain.audit.AuditAction;
import co.edu.uco.ucokudos.domain.audit.AuditEvent;
import co.edu.uco.ucokudos.domain.exception.InvalidCredentialsException;
import co.edu.uco.ucokudos.services.input.LoginCommand;
import co.edu.uco.ucokudos.services.output.ports.repositories.AuditRepository;
import co.edu.uco.ucokudos.services.output.ports.repositories.UserRepository;
import co.edu.uco.ucokudos.services.output.ports.security.PasswordHasher;
import co.edu.uco.ucokudos.services.output.ports.security.TokenIssuer;
import co.edu.uco.ucokudos.services.usecases.auth.LoginInteractor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginInteractorTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordHasher hasher = mock(PasswordHasher.class);
    private final TokenIssuer tokens = mock(TokenIssuer.class);
    private final AuditRepository audit = mock(AuditRepository.class);
    private final LoginInteractor interactor = new LoginInteractor(users, hasher, tokens, audit, Clock.systemUTC());

    @Test
    void emiteTokenConCredencialesValidas() {
        var user = TestData.docente();
        when(users.findCredentialsByEmail("docente@uco.edu.co"))
                .thenReturn(Optional.of(new UserRepository.UserCredentials(user, "hash")));
        when(hasher.matches("secreta", "hash")).thenReturn(true);
        when(tokens.issue(user)).thenReturn("jwt");

        var result = interactor.execute(new LoginCommand("  Docente@UCO.edu.co ", "secreta"));

        assertThat(result.accessToken()).isEqualTo("jwt");
        assertThat(result.user()).isEqualTo(user);
    }

    @Test
    void rechazaContrasenaIncorrectaYAuditaElIntento() {
        when(users.findCredentialsByEmail(anyString()))
                .thenReturn(Optional.of(new UserRepository.UserCredentials(TestData.docente(), "hash")));
        when(hasher.matches("mala", "hash")).thenReturn(false);

        assertThatThrownBy(() -> interactor.execute(new LoginCommand("docente@uco.edu.co", "mala")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Credenciales inválidas");

        var captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(audit).record(captor.capture());
        assertThat(captor.getValue().action()).isEqualTo(AuditAction.LOGIN_FAILED);
    }

    @Test
    void usaElMismoMensajeSiElCorreoNoExiste() {
        when(users.findCredentialsByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.execute(new LoginCommand("noexiste@uco.edu.co", "x")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Credenciales inválidas");
    }
}

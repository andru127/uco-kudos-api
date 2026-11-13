package co.edu.uco.ucokudos.services;

import co.edu.uco.ucokudos.TestData;
import co.edu.uco.ucokudos.domain.audit.AuditAction;
import co.edu.uco.ucokudos.domain.audit.AuditEvent;
import co.edu.uco.ucokudos.domain.exception.AccessDeniedDomainException;
import co.edu.uco.ucokudos.domain.exception.DomainException;
import co.edu.uco.ucokudos.domain.exception.ResourceNotFoundException;
import co.edu.uco.ucokudos.domain.user.Role;
import co.edu.uco.ucokudos.domain.user.UserDomain;
import co.edu.uco.ucokudos.services.input.AuditQuery;
import co.edu.uco.ucokudos.services.input.CreateCategoryCommand;
import co.edu.uco.ucokudos.services.input.UpdateContactCommand;
import co.edu.uco.ucokudos.services.output.ports.repositories.AuditRepository;
import co.edu.uco.ucokudos.services.output.ports.repositories.CategoryRepository;
import co.edu.uco.ucokudos.services.output.ports.repositories.UserRepository;
import co.edu.uco.ucokudos.services.usecases.audit.ListAuditInteractor;
import co.edu.uco.ucokudos.services.usecases.category.CreateCategoryInteractor;
import co.edu.uco.ucokudos.services.usecases.user.GetUserInteractor;
import co.edu.uco.ucokudos.services.usecases.user.UpdateContactInteractor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserCategoryAuditInteractorsTest {

    private final UserRepository users = mock(UserRepository.class);
    private final CategoryRepository categories = mock(CategoryRepository.class);
    private final AuditRepository audit = mock(AuditRepository.class);
    private final Clock clock = Clock.systemUTC();

    private final UserDomain rector = new UserDomain(UUID.randomUUID(), "Rectoría", "rector@uco.edu.co",
            "+573000000002", Role.RECTOR, true);

    @Test
    void actualizaElContactoNormalizandoElCorreoYAudita() {
        var docente = TestData.docente();
        when(users.findById(docente.id())).thenReturn(Optional.of(docente));
        var interactor = new UpdateContactInteractor(users, audit, clock);

        var updated = interactor.execute(new UpdateContactCommand(docente.id(), " Nuevo@UCO.edu.co ", "+573005556677"));

        assertThat(updated.email()).isEqualTo("nuevo@uco.edu.co");
        verify(users).updateContact(updated);
        var captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(audit).record(captor.capture());
        assertThat(captor.getValue().action()).isEqualTo(AuditAction.CONTACT_UPDATED);
    }

    @Test
    void fallaSiElUsuarioNoExiste() {
        when(users.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new GetUserInteractor(users).execute(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void unDocenteNoPuedeCrearCategorias() {
        var docente = TestData.docente();
        when(users.findById(docente.id())).thenReturn(Optional.of(docente));
        var interactor = new CreateCategoryInteractor(users, categories, audit, clock);

        assertThatThrownBy(() -> interactor.execute(new CreateCategoryCommand(docente.id(), "Nueva", null)))
                .isInstanceOf(AccessDeniedDomainException.class);
        verify(categories, never()).create(any());
    }

    @Test
    void noSePuedenCrearCategoriasDuplicadas() {
        when(users.findById(rector.id())).thenReturn(Optional.of(rector));
        when(categories.existsByName("Liderazgo")).thenReturn(true);
        var interactor = new CreateCategoryInteractor(users, categories, audit, clock);

        assertThatThrownBy(() -> interactor.execute(new CreateCategoryCommand(rector.id(), "Liderazgo", null)))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void elRectorCreaUnaCategoriaYQuedaAuditada() {
        when(users.findById(rector.id())).thenReturn(Optional.of(rector));
        var interactor = new CreateCategoryInteractor(users, categories, audit, clock);

        var category = interactor.execute(new CreateCategoryCommand(rector.id(), "Excelencia", "Docencia"));

        verify(categories).create(category);
        verify(audit).record(any(AuditEvent.class));
    }

    @Test
    void unEstudianteNoPuedeConsultarLaAuditoria() {
        var estudiante = TestData.estudiante();
        when(users.findById(estudiante.id())).thenReturn(Optional.of(estudiante));

        assertThatThrownBy(() -> new ListAuditInteractor(users, audit).execute(new AuditQuery(estudiante.id(), null)))
                .isInstanceOf(AccessDeniedDomainException.class);
    }

    @Test
    void elRectorConsultaLaAuditoria() {
        when(users.findById(rector.id())).thenReturn(Optional.of(rector));
        when(audit.findRecent(any(), anyInt())).thenReturn(List.of());

        var events = new ListAuditInteractor(users, audit).execute(new AuditQuery(rector.id(), null));

        assertThat(events).isEmpty();
    }
}

package co.edu.uco.ucokudos.services;

import co.edu.uco.ucokudos.TestData;
import co.edu.uco.ucokudos.domain.audit.AuditAction;
import co.edu.uco.ucokudos.domain.audit.AuditEvent;
import co.edu.uco.ucokudos.domain.category.CategoryDomain;
import co.edu.uco.ucokudos.domain.exception.DomainException;
import co.edu.uco.ucokudos.domain.exception.ResourceNotFoundException;
import co.edu.uco.ucokudos.domain.kudo.KudoDomain;
import co.edu.uco.ucokudos.domain.user.UserDomain;
import co.edu.uco.ucokudos.services.input.PublishKudoCommand;
import co.edu.uco.ucokudos.services.output.ports.events.KudoEventPublisher;
import co.edu.uco.ucokudos.services.output.ports.events.KudoPublishedEvent;
import co.edu.uco.ucokudos.services.output.ports.repositories.AuditRepository;
import co.edu.uco.ucokudos.services.output.ports.repositories.CategoryRepository;
import co.edu.uco.ucokudos.services.output.ports.repositories.KudoRepository;
import co.edu.uco.ucokudos.services.output.ports.repositories.UserRepository;
import co.edu.uco.ucokudos.services.output.ports.transaction.TransactionRunner;
import co.edu.uco.ucokudos.services.usecases.kudo.PublishKudoInteractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PublishKudoInteractorTest {

    private final UserRepository users = mock(UserRepository.class);
    private final CategoryRepository categories = mock(CategoryRepository.class);
    private final KudoRepository kudos = mock(KudoRepository.class);
    private final AuditRepository audit = mock(AuditRepository.class);
    private final KudoEventPublisher events = mock(KudoEventPublisher.class);
    private final TransactionRunner tx = new TransactionRunner() {
        @Override
        public <T> T inTransaction(Supplier<T> work) {
            return work.get();
        }
    };
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-12T15:00:00Z"), ZoneOffset.UTC);

    private PublishKudoInteractor interactor;
    private final UserDomain author = TestData.docente();
    private final UserDomain recipient = TestData.estudiante();
    private final CategoryDomain category = TestData.liderazgo();

    @BeforeEach
    void setUp() {
        interactor = new PublishKudoInteractor(users, categories, kudos, audit, events, tx, clock);
        when(users.findById(author.id())).thenReturn(Optional.of(author));
        when(users.findById(recipient.id())).thenReturn(Optional.of(recipient));
        when(categories.findById(category.id())).thenReturn(Optional.of(category));
    }

    @Test
    void persisteAuditaYPublicaElEvento() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};
        var command = new PublishKudoCommand(author.id(), recipient.id(), category.id(),
                "¡Gran liderazgo!", List.of(new PublishKudoCommand.AttachmentInput("foto.png", "image/png", png)));

        KudoDomain kudo = interactor.execute(command);

        verify(kudos).create(kudo);
        var auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(audit).record(auditCaptor.capture());
        assertThat(auditCaptor.getValue().action()).isEqualTo(AuditAction.KUDO_PUBLISHED);
        assertThat(auditCaptor.getValue().resourceId()).isEqualTo(kudo.id());

        var eventCaptor = ArgumentCaptor.forClass(KudoPublishedEvent.class);
        verify(events).publish(eventCaptor.capture());
        var notification = eventCaptor.getValue().notification();
        assertThat(notification.recipientEmail()).isEqualTo(recipient.email());
        assertThat(notification.recipientPhone()).isEqualTo(recipient.phone());
        assertThat(kudo.attachments()).singleElement()
                .satisfies(a -> assertThat(a.sha256()).hasSize(64));
    }

    @Test
    void noPersisteNiNotificaSiSeIncumpleUnaRegla() {
        var command = new PublishKudoCommand(author.id(), recipient.id(), category.id(), " ",
                List.of());

        assertThatThrownBy(() -> interactor.execute(command)).isInstanceOf(DomainException.class);

        verify(kudos, never()).create(any());
        verifyNoInteractions(events, audit);
    }

    @Test
    void fallaSiElDestinatarioNoExiste() {
        var command = new PublishKudoCommand(author.id(), UUID.randomUUID(), category.id(), "Hola",
                List.of());

        assertThatThrownBy(() -> interactor.execute(command)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(kudos, events);
    }

}

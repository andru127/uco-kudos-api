package co.edu.uco.ucokudos.services.usecases.kudo;

import co.edu.uco.ucokudos.domain.audit.AuditAction;
import co.edu.uco.ucokudos.domain.audit.AuditEvent;
import co.edu.uco.ucokudos.domain.category.CategoryDomain;
import co.edu.uco.ucokudos.domain.exception.ResourceNotFoundException;
import co.edu.uco.ucokudos.domain.kudo.AttachmentDomain;
import co.edu.uco.ucokudos.domain.kudo.KudoDomain;
import co.edu.uco.ucokudos.domain.notification.KudoNotification;
import co.edu.uco.ucokudos.domain.user.UserDomain;
import co.edu.uco.ucokudos.services.input.PublishKudoCommand;
import co.edu.uco.ucokudos.services.output.ports.events.KudoEventPublisher;
import co.edu.uco.ucokudos.services.output.ports.events.KudoPublishedEvent;
import co.edu.uco.ucokudos.services.output.ports.repositories.AuditRepository;
import co.edu.uco.ucokudos.services.output.ports.repositories.CategoryRepository;
import co.edu.uco.ucokudos.services.output.ports.repositories.KudoRepository;
import co.edu.uco.ucokudos.services.output.ports.repositories.UserRepository;
import co.edu.uco.ucokudos.services.output.ports.transaction.TransactionRunner;
import co.edu.uco.ucokudos.services.usecases.UseCaseWithReturn;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

public class PublishKudoInteractor implements UseCaseWithReturn<PublishKudoCommand, KudoDomain> {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final KudoRepository kudoRepository;
    private final AuditRepository auditRepository;
    private final KudoEventPublisher eventPublisher;
    private final TransactionRunner transactionRunner;
    private final Clock clock;

    public PublishKudoInteractor(UserRepository userRepository, CategoryRepository categoryRepository,
                                 KudoRepository kudoRepository, AuditRepository auditRepository,
                                 KudoEventPublisher eventPublisher, TransactionRunner transactionRunner,
                                 Clock clock) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.kudoRepository = kudoRepository;
        this.auditRepository = auditRepository;
        this.eventPublisher = eventPublisher;
        this.transactionRunner = transactionRunner;
        this.clock = clock;
    }

    @Override
    public KudoDomain execute(PublishKudoCommand command) {
        UserDomain author = findUser(command.authorId());
        UserDomain recipient = findUser(command.recipientId());
        CategoryDomain category = categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", command.categoryId()));
        List<AttachmentDomain> attachments = toAttachments(command.attachments());
        Instant now = clock.instant();

        KudoDomain kudo = KudoDomain.publish(author, recipient, category, command.content(), attachments, now);

        return transactionRunner.inTransaction(() -> {
            kudoRepository.create(kudo);
            auditRepository.record(AuditEvent.of(author.id(), AuditAction.KUDO_PUBLISHED, "KUDO", kudo.id(),
                    "Para " + recipient.fullName() + " | categoría " + category.name()
                            + " | adjuntos " + attachments.size(), now));
            eventPublisher.publish(new KudoPublishedEvent(toNotification(kudo)));
            return kudo;
        });
    }

    private UserDomain findUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    private static List<AttachmentDomain> toAttachments(List<PublishKudoCommand.AttachmentInput> inputs) {
        if (inputs == null) {
            return List.of();
        }
        return inputs.stream()
                .map(input -> new AttachmentDomain(UUID.randomUUID(), input.fileName(), input.contentType(),
                        input.content().length, sha256(input.content()), input.content()))
                .toList();
    }

    private static KudoNotification toNotification(KudoDomain kudo) {
        return new KudoNotification(kudo.id(), kudo.recipient().id(), kudo.recipient().fullName(),
                kudo.recipient().email(), kudo.recipient().phone(), kudo.author().fullName(),
                kudo.category().name(), KudoNotification.preview(kudo.content()));
    }

    static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en la JVM", e);
        }
    }
}

package co.edu.uco.ucokudos.domain.kudo;

import co.edu.uco.ucokudos.domain.category.CategoryDomain;
import co.edu.uco.ucokudos.domain.exception.DomainException;
import co.edu.uco.ucokudos.domain.user.UserDomain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record KudoDomain(UUID id, UserDomain author, UserDomain recipient, CategoryDomain category,
                         String content, Instant publishedAt, List<AttachmentDomain> attachments) {

    public static final int MAX_ATTACHMENTS = 5;

    public KudoDomain {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(author, "author");
        Objects.requireNonNull(recipient, "recipient");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(publishedAt, "publishedAt");
        attachments = attachments == null ? List.of() : List.copyOf(attachments);
    }

    public static KudoDomain publish(UserDomain author, UserDomain recipient, CategoryDomain category,
                                     String content, List<AttachmentDomain> attachments, Instant now) {
        if (!author.active()) {
            throw new DomainException("El autor no tiene una cuenta activa");
        }
        if (!recipient.active()) {
            throw new DomainException("El destinatario no tiene una cuenta activa");
        }
        if (author.id().equals(recipient.id())) {
            throw new DomainException("No puedes darte un Kudo a ti mismo");
        }
        if (!category.active()) {
            throw new DomainException("La categoría seleccionada no está activa");
        }
        if (content == null || content.isBlank()) {
            throw new DomainException("El contenido del Kudo es obligatorio");
        }
        if (attachments != null && attachments.size() > MAX_ATTACHMENTS) {
            throw new DomainException("Un Kudo admite como máximo " + MAX_ATTACHMENTS + " adjuntos");
        }
        return new KudoDomain(UUID.randomUUID(), author, recipient, category, content.strip(), now, attachments);
    }
}

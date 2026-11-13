package co.edu.uco.ucokudos.infrastructure.output.repositories.structured.mapper;

import co.edu.uco.ucokudos.domain.kudo.AttachmentDomain;
import co.edu.uco.ucokudos.domain.kudo.KudoDomain;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.AttachmentMetadata;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities.KudoAttachmentEntityJPA;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities.KudoEntityJPA;

import java.util.List;

public final class KudoEntityMapper {

    private KudoEntityMapper() {
    }

    public static KudoDomain toDomain(KudoEntityJPA entity, List<AttachmentMetadata> attachments) {
        return new KudoDomain(entity.getId(),
                UserEntityMapper.toDomain(entity.getAuthor()),
                UserEntityMapper.toDomain(entity.getRecipient()),
                CategoryEntityMapper.toDomain(entity.getCategory()),
                entity.getContent(),
                entity.getPublishedAt(),
                attachments.stream().map(KudoEntityMapper::toDomain).toList());
    }

    public static AttachmentDomain toDomain(AttachmentMetadata metadata) {
        return new AttachmentDomain(metadata.id(), metadata.fileName(), metadata.contentType(),
                metadata.sizeBytes(), metadata.sha256(), null);
    }

    public static AttachmentDomain toDomain(KudoAttachmentEntityJPA entity) {
        return new AttachmentDomain(entity.getId(), entity.getFileName(), entity.getContentType(),
                entity.getSizeBytes(), entity.getSha256(), entity.getContent());
    }
}

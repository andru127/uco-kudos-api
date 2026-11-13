package co.edu.uco.ucokudos.infrastructure.input.rest.dto;

import co.edu.uco.ucokudos.domain.kudo.AttachmentDomain;
import co.edu.uco.ucokudos.domain.kudo.KudoDomain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record KudoResponse(UUID id, UserSummaryResponse author, UserSummaryResponse recipient,
                           CategoryResponse category, String content, Instant publishedAt,
                           List<AttachmentResponse> attachments) {

    public static KudoResponse from(KudoDomain kudo) {
        return new KudoResponse(kudo.id(), UserSummaryResponse.from(kudo.author()),
                UserSummaryResponse.from(kudo.recipient()), CategoryResponse.from(kudo.category()),
                kudo.content(), kudo.publishedAt(),
                kudo.attachments().stream().map(a -> AttachmentResponse.from(kudo.id(), a)).toList());
    }

    public record AttachmentResponse(UUID id, String fileName, String contentType, long sizeBytes, String sha256,
                                     String downloadPath) {

        static AttachmentResponse from(UUID kudoId, AttachmentDomain attachment) {
            return new AttachmentResponse(attachment.id(), attachment.fileName(), attachment.contentType(),
                    attachment.sizeBytes(), attachment.sha256(),
                    "/api/v1/kudos/" + kudoId + "/attachments/" + attachment.id());
        }
    }
}

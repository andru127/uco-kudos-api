package co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa;

import java.util.UUID;

public record AttachmentMetadata(UUID id, UUID kudoId, String fileName, String contentType, long sizeBytes,
                                 String sha256) {
}

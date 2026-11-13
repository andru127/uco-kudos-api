package co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa;

import java.util.UUID;

/** Proyección sin el binario: el muro solo necesita metadatos de los adjuntos. */
public record AttachmentMetadata(UUID id, UUID kudoId, String fileName, String contentType, long sizeBytes,
                                 String sha256) {
}

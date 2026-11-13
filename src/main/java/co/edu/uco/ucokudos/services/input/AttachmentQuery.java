package co.edu.uco.ucokudos.services.input;

import java.util.UUID;

public record AttachmentQuery(UUID kudoId, UUID attachmentId) {
}

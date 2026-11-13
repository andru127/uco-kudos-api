package co.edu.uco.ucokudos.services.input;

import java.util.List;
import java.util.UUID;

public record PublishKudoCommand(UUID authorId, UUID recipientId, UUID categoryId, String content,
                                 List<AttachmentInput> attachments) {

    public record AttachmentInput(String fileName, String contentType, byte[] content) {
    }
}

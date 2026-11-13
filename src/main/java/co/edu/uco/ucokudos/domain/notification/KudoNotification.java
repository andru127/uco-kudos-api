package co.edu.uco.ucokudos.domain.notification;

import java.util.UUID;

public record KudoNotification(UUID kudoId, UUID recipientId, String recipientName, String recipientEmail,
                               String recipientPhone, String authorName, String categoryName, String contentPreview) {

    private static final int PREVIEW_LENGTH = 140;

    public static String preview(String content) {
        return content.length() <= PREVIEW_LENGTH ? content : content.substring(0, PREVIEW_LENGTH) + "…";
    }

    public String title() {
        return "¡Recibiste un Kudo de " + authorName + "!";
    }

    public String body() {
        return "Categoría: " + categoryName + ". \"" + contentPreview + "\"";
    }
}

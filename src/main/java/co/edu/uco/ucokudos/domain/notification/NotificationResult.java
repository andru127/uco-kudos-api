package co.edu.uco.ucokudos.domain.notification;

public record NotificationResult(NotificationChannel channel, NotificationStatus status, String detail) {

    public static NotificationResult sent(NotificationChannel channel, String detail) {
        return new NotificationResult(channel, NotificationStatus.SENT, detail);
    }

    public static NotificationResult failed(NotificationChannel channel, String detail) {
        return new NotificationResult(channel, NotificationStatus.FAILED, detail);
    }

    public static NotificationResult skipped(NotificationChannel channel, String detail) {
        return new NotificationResult(channel, NotificationStatus.SKIPPED, detail);
    }
}

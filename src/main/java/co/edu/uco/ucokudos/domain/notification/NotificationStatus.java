package co.edu.uco.ucokudos.domain.notification;

public enum NotificationStatus {
    /** Entregado al proveedor (SMTP, Twilio) o al navegador conectado. */
    SENT,
    /** Falló después de agotar los reintentos. */
    FAILED,
    /** No aplica: canal sin configurar o destinatario no conectado (push). */
    SKIPPED
}

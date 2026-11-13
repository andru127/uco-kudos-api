package co.edu.uco.ucokudos.domain.kudo;

import co.edu.uco.ucokudos.domain.exception.DomainException;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Archivo adjunto a un Kudo. La lista blanca de tipos y el tamaño máximo son reglas de seguridad
 * del negocio (DRV-004 y DRV-005); el checksum SHA-256 permite verificar integridad y trazabilidad.
 */
public record AttachmentDomain(UUID id, String fileName, String contentType, long sizeBytes,
                               String sha256, byte[] content) {

    public static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;
    public static final Set<String> ALLOWED_TYPES =
            Set.of("image/png", "image/jpeg", "image/webp", "application/pdf");

    public AttachmentDomain {
        Objects.requireNonNull(id, "id");
        if (fileName == null || fileName.isBlank()) {
            throw new DomainException("El adjunto debe tener nombre");
        }
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new DomainException("Tipo de archivo no permitido: " + contentType + ". Permitidos: PNG, JPEG, WEBP y PDF");
        }
        if (sizeBytes <= 0 || sizeBytes > MAX_SIZE_BYTES) {
            throw new DomainException("Cada adjunto debe pesar como máximo 5 MB");
        }
        fileName = sanitize(fileName);
    }

    /** Elimina rutas y caracteres peligrosos del nombre original (previene path traversal). */
    static String sanitize(String original) {
        String withoutPath = original.replace('\\', '/');
        withoutPath = withoutPath.substring(withoutPath.lastIndexOf('/') + 1);
        String safe = withoutPath.replaceAll("[^\\w.\\- ]", "_");
        return safe.length() > 150 ? safe.substring(safe.length() - 150) : safe;
    }
}

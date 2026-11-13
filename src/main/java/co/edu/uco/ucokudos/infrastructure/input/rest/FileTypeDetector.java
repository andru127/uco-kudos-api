package co.edu.uco.ucokudos.infrastructure.input.rest;

import java.util.Arrays;

/**
 * Detecta el tipo real del archivo por sus "magic bytes". No se confía en el Content-Type ni en la
 * extensión que envía el cliente (OWASP: Unrestricted File Upload).
 */
final class FileTypeDetector {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G'};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PDF = {'%', 'P', 'D', 'F'};
    private static final byte[] RIFF = {'R', 'I', 'F', 'F'};
    private static final byte[] WEBP = {'W', 'E', 'B', 'P'};

    private FileTypeDetector() {
    }

    /** @return el MIME detectado o "application/octet-stream" si no es un tipo reconocido. */
    static String detect(byte[] content) {
        if (startsWith(content, 0, PNG)) {
            return "image/png";
        }
        if (startsWith(content, 0, JPEG)) {
            return "image/jpeg";
        }
        if (startsWith(content, 0, PDF)) {
            return "application/pdf";
        }
        if (startsWith(content, 0, RIFF) && startsWith(content, 8, WEBP)) {
            return "image/webp";
        }
        return "application/octet-stream";
    }

    private static boolean startsWith(byte[] content, int offset, byte[] signature) {
        return content.length >= offset + signature.length
                && Arrays.equals(content, offset, offset + signature.length, signature, 0, signature.length);
    }
}

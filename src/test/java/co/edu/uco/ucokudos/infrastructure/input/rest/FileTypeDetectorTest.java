package co.edu.uco.ucokudos.infrastructure.input.rest;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class FileTypeDetectorTest {

    @Test
    void detectaTiposPorSuFirmaYNoPorLaExtension() {
        assertThat(FileTypeDetector.detect(new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D})).isEqualTo("image/png");
        assertThat(FileTypeDetector.detect(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0})).isEqualTo("image/jpeg");
        assertThat(FileTypeDetector.detect("%PDF-1.7".getBytes(StandardCharsets.US_ASCII))).isEqualTo("application/pdf");
        assertThat(FileTypeDetector.detect("RIFF1234WEBPVP8".getBytes(StandardCharsets.US_ASCII))).isEqualTo("image/webp");
    }

    @Test
    void unEjecutableRenombradoComoPngNoPasa() {
        byte[] ejecutableWindows = {'M', 'Z', (byte) 0x90, 0};

        assertThat(FileTypeDetector.detect(ejecutableWindows)).isEqualTo("application/octet-stream");
    }
}

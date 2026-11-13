package co.edu.uco.ucokudos.domain;

import co.edu.uco.ucokudos.domain.exception.DomainException;
import co.edu.uco.ucokudos.domain.kudo.AttachmentDomain;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttachmentDomainTest {

    @Test
    void eliminaRutasDelNombreParaEvitarPathTraversal() {
        var adjunto = new AttachmentDomain(UUID.randomUUID(), "../../etc/passwd<script>.png", "image/png", 10,
                "hash", new byte[10]);

        assertThat(adjunto.fileName()).isEqualTo("passwd_script_.png");
    }

    @Test
    void rechazaTiposNoPermitidos() {
        assertThatThrownBy(() -> new AttachmentDomain(UUID.randomUUID(), "virus.exe", "application/octet-stream",
                10, "hash", new byte[10]))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void rechazaArchivosDeMasDeCincoMegas() {
        long seisMegas = 6L * 1024 * 1024;

        assertThatThrownBy(() -> new AttachmentDomain(UUID.randomUUID(), "grande.pdf", "application/pdf",
                seisMegas, "hash", null))
                .isInstanceOf(DomainException.class);
    }
}

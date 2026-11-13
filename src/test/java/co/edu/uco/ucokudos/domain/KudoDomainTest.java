package co.edu.uco.ucokudos.domain;

import co.edu.uco.ucokudos.TestData;
import co.edu.uco.ucokudos.domain.category.CategoryDomain;
import co.edu.uco.ucokudos.domain.exception.DomainException;
import co.edu.uco.ucokudos.domain.kudo.AttachmentDomain;
import co.edu.uco.ucokudos.domain.kudo.KudoDomain;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KudoDomainTest {

    private static final Instant NOW = Instant.parse("2026-11-12T15:00:00Z");

    @Test
    void publicaUnKudoValido() {
        var kudo = KudoDomain.publish(TestData.docente(), TestData.estudiante(), TestData.liderazgo(),
                "  Excelente trabajo en el proyecto  ", List.of(), NOW);

        assertThat(kudo.id()).isNotNull();
        assertThat(kudo.content()).isEqualTo("Excelente trabajo en el proyecto");
        assertThat(kudo.publishedAt()).isEqualTo(NOW);
    }

    @Test
    void permiteContenidoSinLimiteDeLongitud() {
        String contenidoLargo = "a".repeat(100_000);

        var kudo = KudoDomain.publish(TestData.docente(), TestData.estudiante(), TestData.liderazgo(),
                contenidoLargo, List.of(), NOW);

        assertThat(kudo.content()).hasSize(100_000);
    }

    @Test
    void rechazaKudoAUnoMismo() {
        var docente = TestData.docente();

        assertThatThrownBy(() -> KudoDomain.publish(docente, docente, TestData.liderazgo(), "Hola", List.of(), NOW))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("ti mismo");
    }

    @Test
    void rechazaContenidoVacio() {
        assertThatThrownBy(() -> KudoDomain.publish(TestData.docente(), TestData.estudiante(), TestData.liderazgo(),
                "   ", List.of(), NOW))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void rechazaDestinatarioInactivo() {
        assertThatThrownBy(() -> KudoDomain.publish(TestData.docente(), TestData.inactivo(), TestData.liderazgo(),
                "Hola", List.of(), NOW))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("destinatario");
    }

    @Test
    void rechazaCategoriaInactiva() {
        var inactiva = new CategoryDomain(UUID.randomUUID(), "Antigua", null, false);

        assertThatThrownBy(() -> KudoDomain.publish(TestData.docente(), TestData.estudiante(), inactiva,
                "Hola", List.of(), NOW))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void rechazaMasDeCincoAdjuntos() {
        var adjunto = new AttachmentDomain(UUID.randomUUID(), "a.png", "image/png", 10, "hash", new byte[10]);
        var seisAdjuntos = Collections.nCopies(6, adjunto);

        assertThatThrownBy(() -> KudoDomain.publish(TestData.docente(), TestData.estudiante(), TestData.liderazgo(),
                "Hola", seisAdjuntos, NOW))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("adjuntos");
    }
}

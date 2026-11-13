package co.edu.uco.ucokudos.domain;

import co.edu.uco.ucokudos.domain.category.CategoryDomain;
import co.edu.uco.ucokudos.domain.exception.DomainException;
import co.edu.uco.ucokudos.domain.notification.KudoNotification;
import co.edu.uco.ucokudos.domain.user.Role;
import co.edu.uco.ucokudos.domain.user.UserDomain;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserAndCategoryDomainTest {

    @Test
    void rechazaCorreoInvalido() {
        assertThatThrownBy(() -> new UserDomain(UUID.randomUUID(), "Ana", "no-es-correo", "+573001234567",
                Role.ESTUDIANTE, true))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void rechazaCelularSinIndicativo() {
        assertThatThrownBy(() -> new UserDomain(UUID.randomUUID(), "Ana", "ana@uco.edu.co", "3001234567",
                Role.ESTUDIANTE, true))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("E.164");
    }

    @Test
    void rechazaNombreVacio() {
        assertThatThrownBy(() -> new UserDomain(UUID.randomUUID(), " ", "ana@uco.edu.co", "+573001234567",
                Role.ESTUDIANTE, true))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void actualizaElContactoConservandoElRestoDeDatos() {
        var user = new UserDomain(UUID.randomUUID(), "Ana", "ana@uco.edu.co", "+573001234567", Role.DOCENTE, true);

        var updated = user.withContact("nueva@uco.edu.co", "+573009999999");

        assertThat(updated.id()).isEqualTo(user.id());
        assertThat(updated.role()).isEqualTo(Role.DOCENTE);
        assertThat(updated.email()).isEqualTo("nueva@uco.edu.co");
        assertThat(updated.phone()).isEqualTo("+573009999999");
    }

    @Test
    void creaCategoriasActivasConNombreLimpio() {
        var category = CategoryDomain.create("  Innovación  ", "Ideas nuevas");

        assertThat(category.name()).isEqualTo("Innovación");
        assertThat(category.active()).isTrue();
    }

    @Test
    void rechazaCategoriaSinNombre() {
        assertThatThrownBy(() -> CategoryDomain.create(" ", null)).isInstanceOf(DomainException.class);
    }

    @Test
    void recortaLaVistaPreviaDeMensajesLargos() {
        assertThat(KudoNotification.preview("corto")).isEqualTo("corto");
        assertThat(KudoNotification.preview("x".repeat(200))).hasSize(141).endsWith("…");
    }

    @Test
    void construyeTituloYCuerpoDeLaNotificacion() {
        var notification = new KudoNotification(UUID.randomUUID(), UUID.randomUUID(), "Ana", "ana@uco.edu.co",
                "+573001234567", "Carlos", "Liderazgo", "¡Gracias!");

        assertThat(notification.title()).contains("Carlos");
        assertThat(notification.body()).contains("Liderazgo").contains("¡Gracias!");
    }
}

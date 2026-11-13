package co.edu.uco.ucokudos.domain;

import co.edu.uco.ucokudos.domain.user.Role;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRADOR", "RECTOR", "COORDINADOR_ACADEMICO"})
    void soloAdministradorRectorYCoordinadorAcademicoAdministranCategorias(Role role) {
        assertThat(role.canManageCategories()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"COORDINADOR_FACULTAD", "COORDINADOR_PROGRAMA", "DOCENTE", "ESTUDIANTE"})
    void losDemasRolesNoAdministranCategorias(Role role) {
        assertThat(role.canManageCategories()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"RECTOR", "COORDINADOR_ACADEMICO"})
    void soloRectorYCoordinadorAcademicoPuedenVetar(Role role) {
        assertThat(role.canVetoContent()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRADOR", "COORDINADOR_FACULTAD", "COORDINADOR_PROGRAMA",
            "DOCENTE", "ESTUDIANTE"})
    void elAdministradorYLosDemasNoPuedenVetar(Role role) {
        assertThat(role.canVetoContent()).isFalse();
    }
}

package co.edu.uco.ucokudos;

import co.edu.uco.ucokudos.domain.category.CategoryDomain;
import co.edu.uco.ucokudos.domain.user.Role;
import co.edu.uco.ucokudos.domain.user.UserDomain;

import java.util.UUID;

/** Object Mother: datos de prueba legibles y reutilizables. */
public final class TestData {

    private TestData() {
    }

    public static UserDomain docente() {
        return new UserDomain(UUID.randomUUID(), "Docente Prueba", "docente@uco.edu.co", "+573001112233",
                Role.DOCENTE, true);
    }

    public static UserDomain estudiante() {
        return new UserDomain(UUID.randomUUID(), "Estudiante Prueba", "estudiante@uco.edu.co", "+573004445566",
                Role.ESTUDIANTE, true);
    }

    public static UserDomain inactivo() {
        return new UserDomain(UUID.randomUUID(), "Usuario Inactivo", "inactivo@uco.edu.co", "+573007778899",
                Role.ESTUDIANTE, false);
    }

    public static CategoryDomain liderazgo() {
        return new CategoryDomain(UUID.randomUUID(), "Liderazgo", "Inspira a otros", true);
    }
}

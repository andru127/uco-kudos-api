package co.edu.uco.ucokudos.domain.user;

import co.edu.uco.ucokudos.domain.exception.DomainException;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public record UserDomain(UUID id, String fullName, String email, String phone, Role role, boolean active) {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE_E164 = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    public UserDomain {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(role, "role");
        if (fullName == null || fullName.isBlank()) {
            throw new DomainException("El nombre del usuario es obligatorio");
        }
        validateEmail(email);
        validatePhone(phone);
    }

    public UserDomain withContact(String newEmail, String newPhone) {
        return new UserDomain(id, fullName, newEmail, newPhone, role, active);
    }

    private static void validateEmail(String email) {
        if (email == null || !EMAIL.matcher(email).matches()) {
            throw new DomainException("Correo electrónico inválido");
        }
    }

    private static void validatePhone(String phone) {
        if (phone == null || !PHONE_E164.matcher(phone).matches()) {
            throw new DomainException("El celular debe estar en formato internacional E.164, por ejemplo +573001234567");
        }
    }
}

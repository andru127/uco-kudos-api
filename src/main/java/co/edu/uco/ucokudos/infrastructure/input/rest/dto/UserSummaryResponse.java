package co.edu.uco.ucokudos.infrastructure.input.rest.dto;

import co.edu.uco.ucokudos.domain.user.UserDomain;

import java.util.UUID;

/** Versión pública del usuario: no expone correo ni celular de terceros. */
public record UserSummaryResponse(UUID id, String fullName, String role) {

    public static UserSummaryResponse from(UserDomain user) {
        return new UserSummaryResponse(user.id(), user.fullName(), user.role().name());
    }
}

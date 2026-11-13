package co.edu.uco.ucokudos.infrastructure.input.rest.dto;

import co.edu.uco.ucokudos.domain.user.UserDomain;

import java.util.UUID;

public record UserResponse(UUID id, String fullName, String email, String phone, String role) {

    public static UserResponse from(UserDomain user) {
        return new UserResponse(user.id(), user.fullName(), user.email(), user.phone(), user.role().name());
    }
}

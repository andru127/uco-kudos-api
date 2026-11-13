package co.edu.uco.ucokudos.infrastructure.input.rest.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn, UserResponse user) {
}

package co.edu.uco.ucokudos.services.input;

import co.edu.uco.ucokudos.domain.user.UserDomain;

public record LoginResult(String accessToken, long expiresInSeconds, UserDomain user) {
}

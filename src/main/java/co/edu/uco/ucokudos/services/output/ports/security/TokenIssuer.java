package co.edu.uco.ucokudos.services.output.ports.security;

import co.edu.uco.ucokudos.domain.user.UserDomain;

public interface TokenIssuer {

    String issue(UserDomain user);

    long expiresInSeconds();
}

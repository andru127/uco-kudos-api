package co.edu.uco.ucokudos.infrastructure.output.security;

import co.edu.uco.ucokudos.domain.user.UserDomain;
import co.edu.uco.ucokudos.infrastructure.config.security.SecurityProperties;
import co.edu.uco.ucokudos.services.output.ports.security.TokenIssuer;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Component
public class JwtTokenIssuer implements TokenIssuer {

    public static final String ISSUER = "uco-kudos-api";

    private final JwtEncoder encoder;
    private final SecurityProperties properties;
    private final Clock clock;

    public JwtTokenIssuer(JwtEncoder encoder, SecurityProperties properties, Clock clock) {
        this.encoder = encoder;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public String issue(UserDomain user) {
        Instant now = clock.instant();
        var claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(user.id().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresInSeconds()))
                .claim("name", user.fullName())
                .claim("roles", List.of(user.role().name()))
                .build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public long expiresInSeconds() {
        return properties.tokenTtl().toSeconds();
    }
}

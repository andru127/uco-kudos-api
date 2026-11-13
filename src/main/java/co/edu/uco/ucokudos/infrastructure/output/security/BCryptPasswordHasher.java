package co.edu.uco.ucokudos.infrastructure.output.security;

import co.edu.uco.ucokudos.services.output.ports.security.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {

    private final PasswordEncoder encoder;

    public BCryptPasswordHasher(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public boolean matches(String rawPassword, String hash) {
        return rawPassword != null && encoder.matches(rawPassword, hash);
    }

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }
}

package co.edu.uco.ucokudos.services.output.ports.security;

public interface PasswordHasher {

    boolean matches(String rawPassword, String hash);

    String hash(String rawPassword);
}

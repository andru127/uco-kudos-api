package co.edu.uco.ucokudos.infrastructure.config.seed;

import co.edu.uco.ucokudos.domain.user.Role;
import co.edu.uco.ucokudos.domain.user.UserDomain;
import co.edu.uco.ucokudos.services.output.ports.repositories.UserRepository;
import co.edu.uco.ucokudos.services.output.ports.security.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Crea un usuario por rol si no existen (idempotente). La contraseña llega por variable de entorno
 * (DEMO_PASSWORD), nunca se escribe en el código ni en las migraciones.
 */
@Component
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final SeedProperties properties;
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public DemoDataSeeder(SeedProperties properties, UserRepository userRepository, PasswordHasher passwordHasher) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            return;
        }
        String hash = passwordHasher.hash(properties.demoPassword());
        demoUsers().stream()
                .filter(user -> userRepository.findById(user.id()).isEmpty())
                .filter(user -> !userRepository.existsByEmail(user.email()))
                .forEach(user -> {
                    userRepository.create(user, hash);
                    LOG.info("Usuario de demostración creado: {} ({})", user.email(), user.role());
                });
    }

    private List<UserDomain> demoUsers() {
        return List.of(
                user("Administrador del Sistema", "admin@uco.edu.co", "+573000000001", Role.ADMINISTRADOR),
                user("Rectoría UCO", "rector@uco.edu.co", "+573000000002", Role.RECTOR),
                user("Coordinación Académica", "coord.academico@uco.edu.co", "+573000000003", Role.COORDINADOR_ACADEMICO),
                user("Decanatura de Ingeniería", "decano.ingenieria@uco.edu.co", "+573000000004", Role.COORDINADOR_FACULTAD),
                user("Coordinación Ing. de Sistemas", "coord.sistemas@uco.edu.co", "+573000000005", Role.COORDINADOR_PROGRAMA),
                user("Docente Demo", "docente@uco.edu.co", "+573000000006", Role.DOCENTE),
                // Inicia sesión con DEMO_RECIPIENT_EMAIL si está definido (así las notificaciones llegan a ti).
                new UserDomain(stableId("estudiante-demo"), "Estudiante Demo",
                        orDefault(properties.recipientEmail(), "estudiante@uco.edu.co"),
                        orDefault(properties.recipientPhone(), "+573000000007"), Role.ESTUDIANTE, true));
    }

    private static UserDomain user(String name, String email, String phone, Role role) {
        return new UserDomain(stableId(email), name, email, phone, role, true);
    }

    private static UUID stableId(String email) {
        return UUID.nameUUIDFromBytes(email.getBytes(StandardCharsets.UTF_8));
    }

    private static String orDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}

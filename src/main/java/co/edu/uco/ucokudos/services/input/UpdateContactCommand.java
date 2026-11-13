package co.edu.uco.ucokudos.services.input;

import java.util.UUID;

public record UpdateContactCommand(UUID userId, String email, String phone) {
}

package co.edu.uco.ucokudos.services.input;

import java.util.UUID;

public record CreateCategoryCommand(UUID actorId, String name, String description) {
}

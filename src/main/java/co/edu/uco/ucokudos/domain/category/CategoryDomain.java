package co.edu.uco.ucokudos.domain.category;

import co.edu.uco.ucokudos.domain.exception.DomainException;

import java.util.Objects;
import java.util.UUID;

/** Categoría de reconocimiento (Empatía, Liderazgo, Espíritu UCO...). */
public record CategoryDomain(UUID id, String name, String description, boolean active) {

    public CategoryDomain {
        Objects.requireNonNull(id, "id");
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de la categoría es obligatorio");
        }
        name = name.trim();
    }

    public static CategoryDomain create(String name, String description) {
        return new CategoryDomain(UUID.randomUUID(), name, description, true);
    }
}

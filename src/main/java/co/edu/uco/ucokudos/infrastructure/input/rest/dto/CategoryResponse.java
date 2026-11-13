package co.edu.uco.ucokudos.infrastructure.input.rest.dto;

import co.edu.uco.ucokudos.domain.category.CategoryDomain;

import java.util.UUID;

public record CategoryResponse(UUID id, String name, String description) {

    public static CategoryResponse from(CategoryDomain category) {
        return new CategoryResponse(category.id(), category.name(), category.description());
    }
}

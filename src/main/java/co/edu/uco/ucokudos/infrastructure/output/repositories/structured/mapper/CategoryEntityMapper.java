package co.edu.uco.ucokudos.infrastructure.output.repositories.structured.mapper;

import co.edu.uco.ucokudos.domain.category.CategoryDomain;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities.CategoryEntityJPA;

public final class CategoryEntityMapper {

    private CategoryEntityMapper() {
    }

    public static CategoryDomain toDomain(CategoryEntityJPA entity) {
        return new CategoryDomain(entity.getId(), entity.getName(), entity.getDescription(), entity.isActive());
    }

    public static CategoryEntityJPA toEntity(CategoryDomain category) {
        return new CategoryEntityJPA(category.id(), category.name(), category.description(), category.active());
    }
}

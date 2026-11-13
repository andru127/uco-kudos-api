package co.edu.uco.ucokudos.services.output.ports.repositories;

import co.edu.uco.ucokudos.domain.category.CategoryDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {

    List<CategoryDomain> findAllActive();

    Optional<CategoryDomain> findById(UUID id);

    boolean existsByName(String name);

    void create(CategoryDomain category);
}

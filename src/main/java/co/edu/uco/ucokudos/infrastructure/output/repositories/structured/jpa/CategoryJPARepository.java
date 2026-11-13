package co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa;

import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities.CategoryEntityJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryJPARepository extends JpaRepository<CategoryEntityJPA, UUID> {

    List<CategoryEntityJPA> findByActiveTrueOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}

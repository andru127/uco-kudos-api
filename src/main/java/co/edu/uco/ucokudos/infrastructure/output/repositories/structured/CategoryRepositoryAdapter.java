package co.edu.uco.ucokudos.infrastructure.output.repositories.structured;

import co.edu.uco.ucokudos.domain.category.CategoryDomain;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.CategoryJPARepository;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.mapper.CategoryEntityMapper;
import co.edu.uco.ucokudos.services.output.ports.repositories.CategoryRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Las categorías casi nunca cambian y se leen en cada publicación: candidatas ideales para caché. */
@Component
public class CategoryRepositoryAdapter implements CategoryRepository {

    public static final String CACHE = "categories";

    private final CategoryJPARepository repository;

    public CategoryRepositoryAdapter(CategoryJPARepository repository) {
        this.repository = repository;
    }

    @Override
    @Cacheable(CACHE)
    @Transactional(readOnly = true)
    public List<CategoryDomain> findAllActive() {
        return repository.findByActiveTrueOrderByNameAsc().stream().map(CategoryEntityMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDomain> findById(UUID id) {
        return repository.findById(id).map(CategoryEntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    @Override
    @CacheEvict(cacheNames = CACHE, allEntries = true)
    @Transactional
    public void create(CategoryDomain category) {
        repository.save(CategoryEntityMapper.toEntity(category));
    }
}

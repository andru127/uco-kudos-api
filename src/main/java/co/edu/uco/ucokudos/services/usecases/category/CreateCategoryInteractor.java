package co.edu.uco.ucokudos.services.usecases.category;

import co.edu.uco.ucokudos.domain.audit.AuditAction;
import co.edu.uco.ucokudos.domain.audit.AuditEvent;
import co.edu.uco.ucokudos.domain.category.CategoryDomain;
import co.edu.uco.ucokudos.domain.exception.AccessDeniedDomainException;
import co.edu.uco.ucokudos.domain.exception.DomainException;
import co.edu.uco.ucokudos.domain.exception.ResourceNotFoundException;
import co.edu.uco.ucokudos.services.input.CreateCategoryCommand;
import co.edu.uco.ucokudos.services.output.ports.repositories.AuditRepository;
import co.edu.uco.ucokudos.services.output.ports.repositories.CategoryRepository;
import co.edu.uco.ucokudos.services.output.ports.repositories.UserRepository;
import co.edu.uco.ucokudos.services.usecases.UseCaseWithReturn;

import java.time.Clock;

public class CreateCategoryInteractor implements UseCaseWithReturn<CreateCategoryCommand, CategoryDomain> {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AuditRepository auditRepository;
    private final Clock clock;

    public CreateCategoryInteractor(UserRepository userRepository, CategoryRepository categoryRepository,
                                    AuditRepository auditRepository, Clock clock) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.auditRepository = auditRepository;
        this.clock = clock;
    }

    @Override
    public CategoryDomain execute(CreateCategoryCommand command) {
        var actor = userRepository.findById(command.actorId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", command.actorId()));
        if (!actor.role().canManageCategories()) {
            throw new AccessDeniedDomainException("Tu rol no puede administrar categorías");
        }
        var category = CategoryDomain.create(command.name(), command.description());
        if (categoryRepository.existsByName(category.name())) {
            throw new DomainException("Ya existe una categoría con ese nombre");
        }
        categoryRepository.create(category);
        auditRepository.record(AuditEvent.of(actor.id(), AuditAction.CATEGORY_CREATED, "CATEGORY", category.id(),
                category.name(), clock.instant()));
        return category;
    }
}

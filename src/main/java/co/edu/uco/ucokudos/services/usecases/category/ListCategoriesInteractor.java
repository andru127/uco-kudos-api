package co.edu.uco.ucokudos.services.usecases.category;

import co.edu.uco.ucokudos.domain.category.CategoryDomain;
import co.edu.uco.ucokudos.services.output.ports.repositories.CategoryRepository;
import co.edu.uco.ucokudos.services.usecases.UseCaseWithReturn;

import java.util.List;

public class ListCategoriesInteractor implements UseCaseWithReturn<Void, List<CategoryDomain>> {

    private final CategoryRepository categoryRepository;

    public ListCategoriesInteractor(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryDomain> execute(Void ignored) {
        return categoryRepository.findAllActive();
    }
}

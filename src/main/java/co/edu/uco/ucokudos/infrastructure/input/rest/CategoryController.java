package co.edu.uco.ucokudos.infrastructure.input.rest;

import co.edu.uco.ucokudos.infrastructure.input.rest.dto.CategoryRequest;
import co.edu.uco.ucokudos.infrastructure.input.rest.dto.CategoryResponse;
import co.edu.uco.ucokudos.services.input.CreateCategoryCommand;
import co.edu.uco.ucokudos.services.usecases.category.CreateCategoryInteractor;
import co.edu.uco.ucokudos.services.usecases.category.ListCategoriesInteractor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final ListCategoriesInteractor listCategories;
    private final CreateCategoryInteractor createCategory;

    public CategoryController(ListCategoriesInteractor listCategories, CreateCategoryInteractor createCategory) {
        this.listCategories = listCategories;
        this.createCategory = createCategory;
    }

    @GetMapping
    public List<CategoryResponse> list() {
        return listCategories.execute(null).stream().map(CategoryResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CategoryRequest request) {
        return CategoryResponse.from(createCategory.execute(
                new CreateCategoryCommand(CurrentUser.id(jwt), request.name(), request.description())));
    }
}

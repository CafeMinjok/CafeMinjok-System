package cafe.product.server.presentation.controller;

import cafe.domain.response.ApiResponse;
import cafe.product.server.application.category.CategoryService;
import cafe.product.server.presentation.request.CategoryCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PostMapping
    public ApiResponse<Long> createCategory(@RequestBody @Validated CategoryCreateRequest request) {
        return ApiResponse.created(
                categoryService.createCategory(request.name(), request.parentCategoryId())
        );
    }
}

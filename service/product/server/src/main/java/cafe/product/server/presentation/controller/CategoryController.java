package cafe.product.server.presentation.controller;

import cafe.domain.response.ApiResponse;
import cafe.product.server.application.category.CategoryService;
import cafe.product.server.presentation.request.CategoryCreateRequest;
import cafe.product.server.presentation.request.CategoryUpdateRequest;
import cafe.product.server.presentation.response.CategoryResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable("categoryId") @NotNull Long categoryId,
            @RequestBody @Validated CategoryUpdateRequest request) {
        return ApiResponse.ok(
                categoryService.updateCategory(categoryId, request.name(), request.parentCategoryId())
        );
    }

    @GetMapping("/search")
    public ApiResponse<List<CategoryResponse>> getCategories() {
        return ApiResponse.ok(categoryService.fetchAndCacheCategories());
    }
}

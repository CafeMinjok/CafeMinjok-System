package cafe.product.server.application.category;

import cafe.product.server.domain.model.Category;
import cafe.product.server.domain.repository.jpa.CategoryRepository;
import cafe.product.server.presentation.exception.ProductErrorCode;
import cafe.product.server.presentation.exception.ProductServerException;
import cafe.product.server.presentation.response.CategoryResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j(topic = "CategoryService")
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 카테고리 생성 후 "categories-cache"라는 이름의 캐시에서 키가 'categories'인 항목을 제거
     * 목적: 캐시된 카테고리 목록이 최신 상태를 유지하기 위함
     */
    @Transactional
    @CacheEvict(cacheNames = "categories-cache", key = "'categories'")
    public Long createCategory(String name, Long parentCategoryId) {
        Category parent = Optional.ofNullable(parentCategoryId).map(this::findByCategoryId).orElse(null);
        Category category = new Category(name, parent);
        Optional.ofNullable(parent).ifPresent(p -> p.addSubCategory(category));

        var saved = categoryRepository.save(category);
        return saved.getCategoryId();
    }

    private Category findByCategoryId(Long categoryId) {
        return categoryRepository
                .findByCategoryId(categoryId)
                .orElseThrow(() -> new ProductServerException(ProductErrorCode.NOT_FOUND_CATEGORY));
    }

    @Transactional
    @CacheEvict(cacheNames = "categories-cache", key = "'categories'")
    public CategoryResponse updateCategory(
            Long targetCategoryId, String name, Long parentCategoryId) {
        Category target = findByCategoryId(targetCategoryId);
        Category parent =
                Optional.ofNullable(parentCategoryId).map(this::findByCategoryId).orElse(null);

        target.update(name, parent);
        syncParentCategory(target, parent);
        return CategoryResponse.fromEntity(target);
    }

    private void syncParentCategory(Category target, Category newParent) {
        Category oldParent = target.getParent();
        if (oldParent != null) {
            oldParent.removeSubCategory(target);
        }
        if (newParent != null) {
            newParent.addSubCategory(target);
        }
    }

    @Transactional
    @CacheEvict(cacheNames = "categories-cache", key = "'categories'")
    public void deleteCategory(Long categoryId) {
        Category category = findByCategoryId(categoryId);
        categoryRepository.delete(category);
    }

    @Cacheable(cacheNames = "categories-cache", key = "'categories'")
    public List<CategoryResponse> fetchAndCacheCategories() {
        return categoryRepository.findAllWithSubCategories().stream()
                .filter(category -> category.getParent() == null)
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }
}

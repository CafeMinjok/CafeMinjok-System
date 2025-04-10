package cafe.product.server.application.category;

import cafe.product.server.domain.model.Category;
import cafe.product.server.domain.repository.jpa.CategoryRepository;
import cafe.product.server.presentation.exception.ProductErrorCode;
import cafe.product.server.presentation.exception.ProductServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
}

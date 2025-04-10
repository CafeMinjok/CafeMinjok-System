package cafe.product.server.application.category;

import cafe.product.server.domain.model.Category;
import cafe.product.server.domain.repository.jpa.CategoryRepository;
import cafe.product.server.presentation.exception.ProductErrorCode;
import cafe.product.server.presentation.exception.ProductServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Field;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category parentCategory;

    private void setCategoryId(Category category, Long id) throws Exception {
        Field idField = Category.class.getDeclaredField("categoryId");
        idField.setAccessible(true);
        idField.set(category, id);
    }

    @Test
    void createCategory_withoutParent_success() throws Exception {
        // Given
        String categoryName = "TestCategory";
        Category savedCategory = new Category(categoryName, null);
        setCategoryId(savedCategory, 2L); // ID 설정

        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // When
        Long categoryId = categoryService.createCategory(categoryName, null);

        // Then
        assertNotNull(categoryId);
        assertEquals(2L, categoryId);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_withParent_success() throws Exception {
        // Given
        String categoryName = "ChildCategory";
        Category parentCategory = new Category("Parent", null);
        setCategoryId(parentCategory, 1L);

        when(categoryRepository.findByCategoryId(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0); // 실제 전달된 객체 캡처
            setCategoryId(saved, 2L); // ID 설정
            return saved;
        });

        // When
        Long categoryId = categoryService.createCategory(categoryName, 1L);

        // Then
        assertNotNull(categoryId);
        assertEquals(2L, categoryId);
        verify(categoryRepository, times(1)).findByCategoryId(1L);
        verify(categoryRepository, times(1)).save(any(Category.class));
        assertFalse(parentCategory.getSubCategories().isEmpty());
        assertEquals(categoryName, parentCategory.getSubCategories().get(0).getName());
    }

    @Test
    void createCategory_withInvalidParent_throwsException() {
        // Given
        String categoryName = "TestCategory";
        Long invalidParentId = 999L;

        when(categoryRepository.findByCategoryId(invalidParentId)).thenReturn(Optional.empty());

        // When & Then
        ProductServerException exception = assertThrows(ProductServerException.class, () ->
                categoryService.createCategory(categoryName, invalidParentId)
        );
        assertEquals(ProductErrorCode.NOT_FOUND_CATEGORY, exception.getErrorCode());
        verify(categoryRepository, times(1)).findByCategoryId(invalidParentId);
        verify(categoryRepository, never()).save(any(Category.class));
    }
}
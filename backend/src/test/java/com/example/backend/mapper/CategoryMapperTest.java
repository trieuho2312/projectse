package com.example.backend.mapper;

import com.example.backend.dto.request.CategoryCreationRequest;
import com.example.backend.dto.response.CategoryResponse;
import com.example.backend.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CategoryMapperTest {

    private CategoryMapper categoryMapper;

    @BeforeEach
    void setUp() {
        categoryMapper = new CategoryMapperImpl();
    }

    @Test
    void toCategory_shouldMapCorrectly() {
        // Arrange
        CategoryCreationRequest request = CategoryCreationRequest.builder()
                .name("Electronics")
                .build();

        // Act
        Category category = categoryMapper.toCategory(request);

        // Assert
        assertNotNull(category);
        assertEquals("Electronics", category.getName());
    }

    @Test
    void toCategory_nullRequest_returnsNull() {
        // Act
        Category category = categoryMapper.toCategory(null);

        // Assert
        assertNull(category);
    }

    @Test
    void toCategoryResponse_shouldMapCorrectly() {
        // Arrange
        Category category = Category.builder()
                .categoryId("cat-1")
                .name("Electronics")
                .build();

        // Act
        CategoryResponse response = categoryMapper.toCategoryResponse(category);

        // Assert
        assertNotNull(response);
        assertEquals("cat-1", response.getCategoryId());
        assertEquals("Electronics", response.getName());
    }

    @Test
    void toCategoryResponse_nullCategory_returnsNull() {
        // Act
        CategoryResponse response = categoryMapper.toCategoryResponse(null);

        // Assert
        assertNull(response);
    }
}

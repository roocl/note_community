package org.notes.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.notes.exception.NotFoundException;
import org.notes.mapper.CategoryMapper;
import org.notes.mapper.QuestionMapper;
import org.notes.model.dto.category.CreateCategoryBody;
import org.notes.model.entity.Category;
import org.notes.model.vo.category.CategoryVO;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private QuestionMapper questionMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category parentCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        parentCategory = new Category();
        parentCategory.setCategoryId(1);
        parentCategory.setName("Java");
        parentCategory.setParentCategoryId(0);

        childCategory = new Category();
        childCategory.setCategoryId(2);
        childCategory.setName("Spring");
        childCategory.setParentCategoryId(1);
    }

    @Test
    void categoryList_buildsTree() {
        when(categoryMapper.categoryList()).thenReturn(List.of(parentCategory, childCategory));

        List<CategoryVO> result = categoryService.categoryList();

        assertEquals(1, result.size());
        assertEquals("Java", result.get(0).getName());
        assertEquals(1, result.get(0).getChildren().size());
    }

    @Test
    void deleteCategory_deletesCategoryAndQuestions() {
        when(categoryMapper.findByIdOrParentId(1)).thenReturn(List.of(parentCategory, childCategory));
        when(categoryMapper.deleteByIdBatch(List.of(1, 2))).thenReturn(2);

        categoryService.deleteCategory(1);

        verify(categoryMapper).deleteByIdBatch(List.of(1, 2));
        verify(questionMapper).deleteByCategoryId(1);
    }

    @Test
    void deleteCategory_throwsWhenCategoryMissing() {
        when(categoryMapper.findByIdOrParentId(999)).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class, () -> categoryService.deleteCategory(999));
    }

    @Test
    void createCategory_checksParentBeforeInsert() {
        CreateCategoryBody body = new CreateCategoryBody();
        body.setName("MyBatis");
        body.setParentCategoryId(1);
        when(categoryMapper.findById(1)).thenReturn(parentCategory);

        assertNotNull(categoryService.createCategory(body));

        verify(categoryMapper).insert(any(Category.class));
    }
}

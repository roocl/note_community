package org.notes.service;

import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.category.CreateCategoryBody;
import org.notes.model.dto.category.UpdateCategoryBody;
import org.notes.model.entity.Category;
import org.notes.model.vo.category.CategoryVO;
import org.notes.model.vo.category.CreateCategoryVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * 分类表(Category)表服务接口
 *
 * @author makejava
 * @since 2026-02-04 19:19:32
 */
public interface CategoryService {

    List<CategoryVO> buildCategoryTree();

    ApiResponse<List<CategoryVO>> categoryList();

    ApiResponse<EmptyVO> deleteCategory(Integer categoryId);

    ApiResponse<CreateCategoryVO> createCategory(CreateCategoryBody createCategoryBody);

    ApiResponse<EmptyVO> updateCategory(Integer categoryId, UpdateCategoryBody updateCategoryBody);

    Category findOrCreateCategory(String categoryName);

    Category findOrCreateCategory(String categoryName, Integer parentCategoryId);
}

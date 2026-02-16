package org.notes.service;

import org.notes.model.dto.category.CreateCategoryBody;
import org.notes.model.dto.category.UpdateCategoryBody;
import org.notes.model.entity.Category;
import org.notes.model.vo.category.CategoryVO;
import org.notes.model.vo.category.CreateCategoryVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分类表(Category)表服务接口
 *
 * @author makejava
 * @since 2026-02-04 19:19:32
 */
@Transactional
public interface CategoryService {

    List<CategoryVO> buildCategoryTree();

    List<CategoryVO> categoryList();

    void deleteCategory(Integer categoryId);

    CreateCategoryVO createCategory(CreateCategoryBody createCategoryBody);

    void updateCategory(Integer categoryId, UpdateCategoryBody updateCategoryBody);

    Category findOrCreateCategory(String categoryName, Integer parentCategoryId);
}

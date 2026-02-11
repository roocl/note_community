package org.notes.controller;

import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.category.CreateCategoryBody;
import org.notes.model.dto.category.UpdateCategoryBody;
import org.notes.model.vo.category.CategoryVO;
import org.notes.model.vo.category.CreateCategoryVO;
import org.notes.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * 分类表(Category)表控制层
 *
 * @author makejava
 * @since 2026-02-04 19:19:23
 */
@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/categories")
    public ApiResponse<List<CategoryVO>> userCategories() {
        return categoryService.categoryList();
    }

    @GetMapping("/admin/categories")
    public ApiResponse<List<CategoryVO>> categories() {
        return categoryService.categoryList();
    }

    /**
     * 新增数据
     *
     * @param createCategoryBody 实体
     * @return 新增结果
     */
    @PostMapping("/admin/categories")
    public ApiResponse<CreateCategoryVO> createCategory(
            @Valid @RequestBody CreateCategoryBody createCategoryBody) {
        return categoryService.createCategory(createCategoryBody);
    }

    @PatchMapping("/admin/categories/{categoryId}")
    public ApiResponse<EmptyVO> updateCategory(
            @Min(value = 1, message = "categoryId 必须为正整数") @PathVariable Integer categoryId,
            @Valid @RequestBody UpdateCategoryBody updateCategoryBody) {
        return categoryService.updateCategory(categoryId, updateCategoryBody);
    }

    /**
     * 删除数据
     *
     * @param categoryId 主键
     * @return 删除是否成功
     */
    @DeleteMapping("/admin/categories/{categoryId}")
    public ApiResponse<EmptyVO> deleteCategory(
            @Min(value = 1, message = "categoryId 必须为正整数") @PathVariable Integer categoryId) {
        return categoryService.deleteCategory(categoryId);
    }

}


package org.notes.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.category.CreateCategoryBody;
import org.notes.model.dto.category.UpdateCategoryBody;
import org.notes.model.vo.category.CategoryVO;
import org.notes.model.vo.category.CreateCategoryVO;
import org.notes.service.CategoryService;
import org.notes.utils.ApiResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * 分类表(Category)表控制层
 *
 * @author makejava
 * @since 2026-02-04 19:19:23
 */
@Api(tags = "分类管理")
@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @ApiOperation("获取分类列表（用户端）")
    @GetMapping("/categories")
    public ApiResponse<List<CategoryVO>> userCategories() {
        return ApiResponseUtil.success("获取分类列表成功", categoryService.categoryList());
    }

    @ApiOperation("获取分类列表（管理端）")
    @GetMapping("/admin/categories")
    public ApiResponse<List<CategoryVO>> categories() {
        return ApiResponseUtil.success("获取分类列表成功", categoryService.categoryList());
    }

    @ApiOperation("新增分类")
    @PostMapping("/admin/categories")
    public ApiResponse<CreateCategoryVO> createCategory(
            @Valid @RequestBody CreateCategoryBody createCategoryBody) {
        return ApiResponseUtil.success("创建分类成功", categoryService.createCategory(createCategoryBody));
    }

    @ApiOperation("更新分类")
    @PatchMapping("/admin/categories/{categoryId}")
    public ApiResponse<EmptyVO> updateCategory(
            @ApiParam("分类ID") @Min(value = 1, message = "categoryId 必须为正整数") @PathVariable Integer categoryId,
            @Valid @RequestBody UpdateCategoryBody updateCategoryBody) {
        categoryService.updateCategory(categoryId, updateCategoryBody);
        return ApiResponseUtil.success("更新分类成功");
    }

    @ApiOperation("删除分类")
    @DeleteMapping("/admin/categories/{categoryId}")
    public ApiResponse<EmptyVO> deleteCategory(
            @ApiParam("分类ID") @Min(value = 1, message = "categoryId 必须为正整数") @PathVariable Integer categoryId) {
        categoryService.deleteCategory(categoryId);
        return ApiResponseUtil.success("删除分类成功");
    }

}

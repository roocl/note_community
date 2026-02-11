package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import org.notes.mapper.CategoryMapper;
import org.notes.mapper.QuestionMapper;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.category.CreateCategoryBody;
import org.notes.model.dto.category.UpdateCategoryBody;
import org.notes.model.entity.Category;
import org.notes.model.vo.category.CategoryVO;
import org.notes.model.vo.category.CreateCategoryVO;
import org.notes.service.CategoryService;
import org.notes.utils.ApiResponseUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类表(Category)表服务实现类
 *
 * @author makejava
 * @since 2026-02-04 19:19:32
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    private final QuestionMapper QuestionMapper;
    private final QuestionMapper questionMapper;

    @Override
    public List<CategoryVO> buildCategoryTree() {
        List<Category> categories = categoryMapper.categoryList();
        Map<Integer, CategoryVO> categoryVOMap = new HashMap<>();

        categories.forEach(category -> {
            if (category.getParentCategoryId() == 0) {
                CategoryVO categoryVO = new CategoryVO();
                BeanUtils.copyProperties(category, categoryVO);
                categoryVO.setChildren(new ArrayList<>());
                categoryVOMap.put(category.getCategoryId(), categoryVO);
            } else {
                CategoryVO.ChildrenCategoryVO childrenCategoryVO = new CategoryVO.ChildrenCategoryVO();
                BeanUtils.copyProperties(category, childrenCategoryVO);

                CategoryVO parentCategoryVO = categoryVOMap.get(category.getParentCategoryId());
                if (parentCategoryVO != null) {
                    parentCategoryVO.getChildren().add(childrenCategoryVO);
                }
            }
        }
        );
        return new ArrayList<>(categoryVOMap.values());
    }

    @Override
    public ApiResponse<List<CategoryVO>> categoryList() {
        return ApiResponseUtil.success("获取分类列表成功", buildCategoryTree());
    }

    @Override
    public ApiResponse<EmptyVO> deleteCategory(Integer categoryId) {
        List<Category> categories = categoryMapper.findByIdOrParentId(categoryId);

        if (categories.isEmpty()) {
            return ApiResponseUtil.error("分类Id非法");
        }

        List<Integer> categoryIds = categories.stream()
                .map(Category::getCategoryId)
                .toList();

        try {
            int deleteCount = categoryMapper.deleteByIdBatch(categoryIds);
            if (deleteCount != categoryIds.size()) {
                throw new RuntimeException("删除分类失败");
            }
            questionMapper.deleteByCategoryId(categoryId);
            
            return ApiResponseUtil.success("删除分类成功");
        } catch (Exception e) {
            throw new RuntimeException("删除分类失败");
        }
    }

    @Override
    public ApiResponse<CreateCategoryVO> createCategory(CreateCategoryBody createCategoryBody) {
        if (createCategoryBody.getParentCategoryId() != 0) {
            Category parent = categoryMapper.findById(createCategoryBody.getParentCategoryId());
            if (parent == null) {
                return ApiResponseUtil.error("父分类Id不存在");
            }
        }

        Category category = new Category();
        BeanUtils.copyProperties(createCategoryBody, category);

        try {
            categoryMapper.insert(category);
            CreateCategoryVO createCategoryVO = new CreateCategoryVO();
            createCategoryVO.setCategoryId(category.getCategoryId());
            return ApiResponseUtil.success("创建分类成功", createCategoryVO);
        } catch (Exception e) {
            return ApiResponseUtil.error("创建分类失败");
        }
    }

    @Override
    public ApiResponse<EmptyVO> updateCategory(Integer categoryId, UpdateCategoryBody updateCategoryBody) {
        Category category = categoryMapper.findById(categoryId);

        if (category == null) {
            return ApiResponseUtil.error("分类Id不存在");
        }

        category.setName(updateCategoryBody.getName());

        try {
            categoryMapper.update(category);
            return ApiResponseUtil.success("更新分类成功");
        } catch (Exception e) {
            return ApiResponseUtil.error("更新分类失败");
        }
    }

    @Override
    public Category findOrCreateCategory(String categoryName) {
        //todo 将两个方法合并?
        Category category = categoryMapper.findByName(categoryName);

        if (category != null) {
            return category;
        }

        try {
            Category newCategory = new Category();
            newCategory.setName(categoryName.trim());
            newCategory.setParentCategoryId(0);
            categoryMapper.insert(newCategory);
            return newCategory;
        } catch (Exception e) {
            throw new RuntimeException("创建分类失败");
        }
    }

    @Override
    public Category findOrCreateCategory(String categoryName, Integer parentCategoryId) {
        Category category = categoryMapper.findByName(categoryName);

        if (category != null) {
            return category;
        }

        try {
            Category newCategory = new Category();
            newCategory.setName(categoryName.trim());
            newCategory.setParentCategoryId(parentCategoryId);
            categoryMapper.insert(newCategory);
            return newCategory;
        } catch (Exception e) {
            throw new RuntimeException("创建分类失败");
        }
    }
}

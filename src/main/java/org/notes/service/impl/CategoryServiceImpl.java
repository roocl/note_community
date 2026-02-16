package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import org.notes.exception.BaseException;
import org.notes.exception.NotFoundException;
import org.notes.mapper.CategoryMapper;
import org.notes.mapper.QuestionMapper;
import org.notes.model.dto.category.CreateCategoryBody;
import org.notes.model.dto.category.UpdateCategoryBody;
import org.notes.model.entity.Category;
import org.notes.model.vo.category.CategoryVO;
import org.notes.model.vo.category.CreateCategoryVO;
import org.notes.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        });
        return new ArrayList<>(categoryVOMap.values());
    }

    @Override
    public List<CategoryVO> categoryList() {
        return buildCategoryTree();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Integer categoryId) {
        List<Category> categories = categoryMapper.findByIdOrParentId(categoryId);

        if (categories.isEmpty()) {
            throw new NotFoundException("分类Id非法");
        }

        List<Integer> categoryIds = categories.stream()
                .map(Category::getCategoryId)
                .toList();

        try {
            int deleteCount = categoryMapper.deleteByIdBatch(categoryIds);
            if (deleteCount != categoryIds.size()) {
                throw new BaseException("删除分类失败");
            }
            questionMapper.deleteByCategoryId(categoryId);
        } catch (Exception e) {
            throw new BaseException("删除分类失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateCategoryVO createCategory(CreateCategoryBody createCategoryBody) {
        if (createCategoryBody.getParentCategoryId() != 0) {
            Category parent = categoryMapper.findById(createCategoryBody.getParentCategoryId());
            if (parent == null) {
                throw new NotFoundException("父分类Id不存在");
            }
        }

        Category category = new Category();
        BeanUtils.copyProperties(createCategoryBody, category);

        try {
            categoryMapper.insert(category);
            CreateCategoryVO createCategoryVO = new CreateCategoryVO();
            createCategoryVO.setCategoryId(category.getCategoryId());
            return createCategoryVO;
        } catch (Exception e) {
            throw new BaseException("创建分类失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Integer categoryId, UpdateCategoryBody updateCategoryBody) {
        Category category = categoryMapper.findById(categoryId);

        if (category == null) {
            throw new NotFoundException("分类Id不存在");
        }

        category.setName(updateCategoryBody.getName());

        try {
            categoryMapper.update(category);
        } catch (Exception e) {
            throw new BaseException("更新分类失败");
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
            newCategory.setParentCategoryId(parentCategoryId != null ? parentCategoryId : 0);
            categoryMapper.insert(newCategory);
            return newCategory;
        } catch (Exception e) {
            throw new BaseException("创建分类失败");
        }
    }
}

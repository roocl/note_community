package org.notes.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.notes.model.entity.Category;

import java.util.List;

/**
* @author Administrator
* @description 针对表【category(分类表)】的数据库操作Mapper
* @createDate 2026-02-04 19:51:31
* @Entity org.notes.model.entity.Category
*/
@Mapper
public interface CategoryMapper {

    int insert(Category category);

    int insertBatch(@Param("categories") List<Category> categories);

    List<Category> categoryList();

    Category findById(Integer categoryId);

    Category findByName(String categoryName);

    List<Category> findByIdBatch(@Param("categoryIds") List<Integer> categoryIds);

    List<Category> findByIdOrParentId(Integer categoryId);

    int deleteById(Integer categoryId);

    int deleteByIdBatch(@Param("categoryIds") List<Integer> categoryIds);

    int update(Category category);

}

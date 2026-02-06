package org.notes.model.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 分类表
 * @TableName category
 */
@Data
public class Category {
    /**
     * 分类 ID
     */
    private Integer categoryId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 上级分类 ID, 为 0 时表示当前分类是一级分类
     */
    private Integer parentCategoryId;

    /**
     * 记录创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 记录更新时间
     */
    private LocalDateTime updatedAt;
}
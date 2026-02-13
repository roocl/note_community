package org.notes.model.entity;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 分类表
 * 
 * @TableName category
 */
@ApiModel("分类")
@Data
public class Category {
    @ApiModelProperty("分类ID")
    private Integer categoryId;

    @ApiModelProperty("分类名称")
    private String name;

    @ApiModelProperty("上级分类ID, 为0时表示一级分类")
    private Integer parentCategoryId;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}
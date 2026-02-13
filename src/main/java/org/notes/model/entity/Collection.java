package org.notes.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel("收藏夹")
@Data
public class Collection {
    @ApiModelProperty("收藏夹ID")
    private Integer collectionId;

    @ApiModelProperty("收藏夹名称")
    private String name;

    @ApiModelProperty("收藏夹描述")
    private String description;

    @ApiModelProperty("创建者ID")
    private Long creatorId;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    private Date updatedAt;
}
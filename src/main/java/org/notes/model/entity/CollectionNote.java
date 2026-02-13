package org.notes.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel("收藏夹笔记关联")
@Data
public class CollectionNote {
    @ApiModelProperty("收藏夹ID")
    private Integer collectionId;

    @ApiModelProperty("笔记ID")
    private Integer noteId;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    private Date updatedAt;
}

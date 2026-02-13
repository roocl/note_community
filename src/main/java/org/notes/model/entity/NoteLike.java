package org.notes.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel("笔记点赞")
@Data
public class NoteLike {
    @ApiModelProperty("笔记ID")
    private Integer noteId;

    @ApiModelProperty("点赞用户ID")
    private Long userId;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    private Date updatedAt;
}
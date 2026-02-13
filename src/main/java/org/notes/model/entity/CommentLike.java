package org.notes.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@ApiModel("评论点赞")
@Data
public class CommentLike {
    @ApiModelProperty("评论点赞ID")
    private Integer commentLikeId;

    @ApiModelProperty("评论ID")
    private Integer commentId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;
}
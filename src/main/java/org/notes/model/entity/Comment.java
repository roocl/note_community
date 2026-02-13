package org.notes.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@ApiModel("评论")
@Data
public class Comment {
    @ApiModelProperty("评论ID")
    private Integer commentId;

    @ApiModelProperty("笔记ID")
    private Integer noteId;

    @ApiModelProperty("作者ID")
    private Long authorId;

    @ApiModelProperty("父评论ID")
    private Integer parentId;

    @ApiModelProperty("评论内容")
    private String content;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("回复数")
    private Integer replyCount;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}

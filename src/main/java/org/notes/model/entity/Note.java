package org.notes.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@ApiModel("笔记")
@Data
public class Note {
    @ApiModelProperty("笔记ID")
    private Integer noteId;

    @ApiModelProperty("作者ID")
    private Long authorId;

    @ApiModelProperty("问题ID")
    private Integer questionId;

    @ApiModelProperty("笔记内容")
    private String content;

    @ApiModelProperty("搜索向量")
    private String searchVector;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("评论数")
    private Integer commentCount;

    @ApiModelProperty("收藏数")
    private Integer collectCount;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}

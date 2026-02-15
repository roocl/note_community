package org.notes.model.vo.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记搜索结果 VO（含高亮）
 */
@ApiModel("笔记搜索结果")
@Data
public class NoteSearchVO {

    @ApiModelProperty("笔记ID")
    private Integer noteId;

    @ApiModelProperty("作者ID")
    private Long authorId;

    @ApiModelProperty("问题ID")
    private Integer questionId;

    @ApiModelProperty("笔记内容（可能包含高亮标签）")
    private String content;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("评论数")
    private Integer commentCount;

    @ApiModelProperty("收藏数")
    private Integer collectCount;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;
}

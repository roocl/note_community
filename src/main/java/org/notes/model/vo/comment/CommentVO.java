package org.notes.model.vo.comment;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.notes.model.vo.user.UserActionVO;

import java.time.LocalDateTime;
import java.util.List;

@ApiModel("评论VO")
@Data
public class CommentVO {
    @ApiModelProperty("评论ID")
    private Integer commentId;

    @ApiModelProperty("笔记ID")
    private Integer noteId;

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

    @ApiModelProperty("作者信息")
    private SimpleAuthorVO author;

    @ApiModelProperty("用户操作信息")
    private UserActionVO userActions;

    @ApiModelProperty("回复列表")
    private List<CommentVO> replies;

    @ApiModel("评论简单作者信息")
    @Data
    public static class SimpleAuthorVO {
        @ApiModelProperty("用户ID")
        private Long userId;

        @ApiModelProperty("用户名")
        private String username;

        @ApiModelProperty("头像URL")
        private String avatarUrl;
    }
}

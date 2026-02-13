package org.notes.model.vo.note;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@ApiModel("笔记详情VO")
@Data
public class NoteVO {
    @ApiModelProperty("笔记ID")
    private Integer noteId;

    @ApiModelProperty("笔记内容")
    private String content;

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

    @ApiModelProperty("作者信息")
    private SimpleAuthorVO author;

    @ApiModelProperty("用户操作状态")
    private UserActionsVO userActionsVO;

    @ApiModelProperty("关联题目信息")
    private SimpleQuestionVO question;

    @ApiModel("笔记简单作者信息")
    @Data
    public static class SimpleAuthorVO {
        @ApiModelProperty("用户ID")
        private Long userId;

        @ApiModelProperty("用户名")
        private String username;

        @ApiModelProperty("头像URL")
        private String avatarUrl;
    }

    @ApiModel("笔记用户操作状态")
    @Data
    public static class UserActionsVO {
        @ApiModelProperty("是否已点赞")
        private Boolean isLiked;

        @ApiModelProperty("是否已收藏")
        private Boolean isCollected;
    }

    @ApiModel("笔记关联题目简要信息")
    @Data
    public static class SimpleQuestionVO {
        @ApiModelProperty("题目ID")
        private Integer questionId;

        @ApiModelProperty("题目标题")
        private String title;

        @ApiModelProperty("题目难度")
        private Integer difficulty;

        @ApiModelProperty("题目考点")
        private String examPoint;
    }
}

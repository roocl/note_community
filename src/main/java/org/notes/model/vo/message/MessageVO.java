package org.notes.model.vo.message;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@ApiModel("消息VO")
@Data
public class MessageVO {
    @ApiModelProperty("消息ID")
    private Integer messageId;

    @ApiModelProperty("发送者信息")
    private Sender sender;

    @ApiModelProperty("消息类型")
    private Integer type;

    @ApiModelProperty("目标信息")
    private Target target;

    @ApiModelProperty("消息内容")
    private String content;

    @ApiModelProperty("是否已读")
    private Boolean isRead;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModel("消息发送者")
    @Data
    public static class Sender {
        @ApiModelProperty("用户ID")
        private Long userId;

        @ApiModelProperty("用户名")
        private String username;

        @ApiModelProperty("头像URL")
        private String avatarUrl;
    }

    @ApiModel("消息目标")
    @Data
    public static class Target {
        @ApiModelProperty("目标ID")
        private Integer targetId;

        @ApiModelProperty("目标类型")
        private Integer targetType;

        @ApiModelProperty("关联题目摘要")
        private QuestionSummary questionSummary;
    }

    @ApiModel("题目摘要")
    @Data
    public static class QuestionSummary {
        @ApiModelProperty("题目ID")
        private Integer questionId;

        @ApiModelProperty("题目标题")
        private String title;
    }
}
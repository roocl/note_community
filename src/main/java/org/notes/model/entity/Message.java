package org.notes.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@ApiModel("消息")
@Data
public class Message {
    @ApiModelProperty("消息ID")
    private Integer messageId;

    @ApiModelProperty("接收者ID")
    private Long receiverId;

    @ApiModelProperty("发送者ID")
    private Long senderId;

    @ApiModelProperty("消息类型")
    private Integer type;

    @ApiModelProperty("目标ID")
    private Integer targetId;

    @ApiModelProperty("目标类型")
    private Integer targetType;

    @ApiModelProperty("消息内容")
    private String content;

    @ApiModelProperty("是否已读")
    private Boolean isRead;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}
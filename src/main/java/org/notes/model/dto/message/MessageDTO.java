package org.notes.model.dto.message;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("消息DTO")
@Data
public class MessageDTO {
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
}

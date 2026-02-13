package org.notes.model.dto.message;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("批量标记已读请求")
@Data
public class ReadMessageBatchRequest {
    @ApiModelProperty("消息ID列表")
    private List<Integer> messageIds;
}

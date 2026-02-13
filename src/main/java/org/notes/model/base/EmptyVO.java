package org.notes.model.base;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("空响应VO")
@Data
public class EmptyVO {
    @ApiModelProperty("是否为空")
    private boolean empty = true;
}

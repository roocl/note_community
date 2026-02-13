package org.notes.model.vo.message;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("按类型统计未读数")
@Data
public class UnreadCountByType {
    @ApiModelProperty("消息类型")
    private String type;

    @ApiModelProperty("未读数量")
    private Integer count;
}

package org.notes.model.dto.message;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;

@ApiModel("消息查询参数")
@Data
public class MessageQueryParams {
    @ApiModelProperty("消息类型")
    private String type;

    @ApiModelProperty("是否已读")
    private Boolean isRead;

    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("页码，默认1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    @ApiModelProperty("每页大小，默认10")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 10;

    @ApiModelProperty("排序字段，默认created_at")
    private String sortField = "created_at";

    @ApiModelProperty("排序方向，默认desc")
    private String sortOrder = "desc";
}
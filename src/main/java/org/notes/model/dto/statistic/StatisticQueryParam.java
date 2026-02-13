package org.notes.model.dto.statistic;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ApiModel("统计数据查询参数")
@Data
public class StatisticQueryParam {
    @ApiModelProperty(value = "页码", required = true, example = "1")
    @NotNull(message = "page 不能为空")
    @Min(value = 1, message = "page 必须为正整数")
    private Integer page;

    @ApiModelProperty(value = "每页大小", required = true, example = "10")
    @NotNull(message = "page 不能为空")
    @Min(value = 1, message = "page 必须为正整数")
    private Integer pageSize;
}

package org.notes.model.vo.note;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("笔记Top3统计")
@Data
public class Top3Count {
    @ApiModelProperty("上月Top3次数")
    private Integer lastMonthTop3Count;

    @ApiModelProperty("本月Top3次数")
    private Integer thisMonthTop3Count;
}
package org.notes.model.base;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@ApiModel("分页信息")
@Data
@AllArgsConstructor
public class Pagination {
    @ApiModelProperty("当前页码")
    private Integer page;

    @ApiModelProperty("每页大小")
    private Integer pageSize;

    @ApiModelProperty("总记录数")
    private Integer total;
}

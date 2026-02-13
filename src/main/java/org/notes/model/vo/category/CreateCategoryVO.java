package org.notes.model.vo.category;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("创建分类结果")
@Data
public class CreateCategoryVO {
    @ApiModelProperty("新建分类ID")
    private Integer categoryId;
}

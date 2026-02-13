package org.notes.model.dto.collection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ApiModel("创建收藏夹请求")
@Data
public class CreateCollectionBody {
    @ApiModelProperty(value = "收藏夹名称", required = true)
    @NotNull(message = "name 不能为空")
    @NotBlank(message = "name 不能为空")
    private String name;

    @ApiModelProperty("收藏夹描述")
    private String description;
}

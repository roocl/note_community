package org.notes.model.dto.category;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ApiModel("更新分类请求")
@Data
public class UpdateCategoryBody {

    @ApiModelProperty(value = "分类名称", required = true)
    @NotBlank(message = "name 不能为空")
    @NotNull(message = "name 不能为空")
    @Length(max = 32, min = 1, message = "name 长度在 1 - 32 之间")
    private String name;
}

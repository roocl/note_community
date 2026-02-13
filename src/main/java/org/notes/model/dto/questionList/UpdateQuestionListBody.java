package org.notes.model.dto.questionList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@ApiModel("更新题单请求")
@Data
public class UpdateQuestionListBody {
    @ApiModelProperty("题单名称")
    @Length(max = 32, message = "name 长度不能超过 32")
    private String name;

    @ApiModelProperty("题单类型: 1 或 2")
    @Range(min = 1, max = 2, message = "type 必须为 1 或 2")
    private Integer type;

    @ApiModelProperty("题单描述")
    @Length(max = 255, message = "description 长度不能超过 255")
    private String description;
}

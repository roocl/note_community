package org.notes.model.dto.questionList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ApiModel("创建题单项请求")
@Data
public class CreateQuestionListItemBody {
    @ApiModelProperty(value = "题单ID", required = true)
    @NotNull(message = "questionListId 不能为空")
    @Min(value = 1, message = "questionListId 必须为正整数")
    private Integer questionListId;

    @ApiModelProperty(value = "题目ID", required = true)
    @NotNull(message = "questionId 不能为空")
    @Min(value = 1, message = "questionId 必须为正整数")
    private Integer questionId;
}
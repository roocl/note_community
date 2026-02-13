package org.notes.model.vo.question;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("创建题目结果")
@Data
public class CreateQuestionVO {
    @ApiModelProperty("新建题目ID")
    private Integer questionId;
}

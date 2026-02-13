package org.notes.model.vo.questionList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("创建题单结果")
@Data
public class CreateQuestionListVO {
    @ApiModelProperty("新建题单ID")
    private Integer questionListId;
}

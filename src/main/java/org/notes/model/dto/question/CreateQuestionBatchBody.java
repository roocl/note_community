package org.notes.model.dto.question;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("批量创建题目请求")
@Data
public class CreateQuestionBatchBody {
    @ApiModelProperty("Markdown格式的题目数据")
    private String markdown;
}
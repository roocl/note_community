package org.notes.model.dto.question;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel("搜索题目请求")
@Data
public class SearchQuestionBody {
    @ApiModelProperty(value = "搜索关键词", required = true)
    @NotNull(message = "keyword 不能为空")
    @NotEmpty(message = "keyword 不能为空")
    @Length(min = 1, max = 32, message = "keyword 长度在 1 和 32 范围内")
    private String keyword;
}

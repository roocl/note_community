package org.notes.model.dto.note;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ApiModel("创建笔记请求")
@Data
public class CreateNoteRequest {
    @ApiModelProperty(value = "题目ID", required = true)
    @NotNull(message = "questionId 不能为空")
    @Min(value = 1, message = "questionId 必须为正整数")
    private Integer questionId;

    @ApiModelProperty(value = "笔记内容", required = true)
    @NotNull(message = "content 不能为空")
    @NotBlank(message = "content 不能为空")
    private String content;
}

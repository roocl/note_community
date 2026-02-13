package org.notes.model.dto.note;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ApiModel("更新笔记请求")
@Data
public class UpdateNoteRequest {
    @ApiModelProperty(value = "笔记内容", required = true)
    @NotNull(message = "content 不能为空")
    @NotBlank(message = "content 不能为空")
    private String content;
}

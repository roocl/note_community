package org.notes.model.dto.comment;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ApiModel("创建评论请求")
@Data
public class CreateCommentRequest {
    @ApiModelProperty(value = "笔记ID", required = true)
    @NotNull(message = "笔记ID不能为空")
    private Integer noteId;

    @ApiModelProperty("父评论ID（回复时填写）")
    private Integer parentId;

    @ApiModelProperty(value = "评论内容", required = true)
    @NotBlank(message = "评论内容不能为空")
    private String content;
}
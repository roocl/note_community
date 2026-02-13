package org.notes.model.dto.comment;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ApiModel("评论查询参数")
@Data
public class CommentQueryParams {
    @ApiModelProperty(value = "笔记ID", required = true)
    @NotNull(message = "笔记ID不能为空")
    private Integer noteId;

    @ApiModelProperty(value = "页码", required = true)
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于0")
    private Integer page;

    @ApiModelProperty(value = "每页大小", required = true)
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize;
}
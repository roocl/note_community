package org.notes.model.dto.question;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@ApiModel("题目查询参数")
@Data
public class QuestionQueryParams {

    @ApiModelProperty("分类ID")
    @Min(value = 1, message = "分类Id必须为正整数")
    private Long categoryId;

    @ApiModelProperty("排序字段: view 或 difficulty")
    @Pattern(regexp = "^(view|difficulty)$", message = "sort必须为view或difficulty")
    private String sort;

    @ApiModelProperty("排序方向: asc 或 desc")
    @Pattern(regexp = "^(asc|desc)$", message = "order必须为asc或desc")
    private String order;

    @ApiModelProperty(value = "页码", required = true, example = "1")
    @NotNull(message = "page不能为空")
    @Min(value = 1, message = "page必须为正整数")
    private Integer page;

    @ApiModelProperty(value = "每页大小", required = true, example = "20")
    @NotNull(message = "pageSize不能为空")
    @Min(value = 1, message = "pageSize必须为正整数")
    @Max(value = 200, message = "pageSize不能超过200")
    private Integer pageSize;
}
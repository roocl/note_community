package org.notes.model.dto.question;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class QuestionQueryParams {

    @Min(value = 1, message = "分类Id必须为正整数")
    private Long categoryId;

    @Pattern(regexp = "^(view|difficulty)$", message = "sort必须为view或difficulty")
    private String sort;

    @Pattern(regexp = "^(asc|desc)$", message = "order必须为asc或desc")
    private String order;

    @NotNull(message = "page不能为空")
    @Min(value = 1, message = "page必须为正整数")
    private Integer page;

    @NotNull(message = "pageSize不能为空")
    @Min(value = 1, message = "pageSize必须为正整数")
    @Max(value = 200, message = "pageSize不能超过200")
    private Integer pageSize;
}
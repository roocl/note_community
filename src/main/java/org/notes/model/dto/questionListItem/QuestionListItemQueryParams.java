package org.notes.model.dto.questionListItem;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ApiModel("题单项查询参数")
@Data
public class QuestionListItemQueryParams {
    @ApiModelProperty(value = "题单ID", required = true)
    @NotNull(message = "questionListId 不能为空")
    @Min(value = 1, message = "questionListId 必须为正整数")
    private Integer questionListId;

    @ApiModelProperty(value = "页码", required = true, example = "1")
    @NotNull(message = "page 不能为空")
    @Min(value = 1, message = "page 必须为正整数")
    private Integer page;

    @ApiModelProperty(value = "每页大小", required = true, example = "20")
    @NotNull(message = "pageSize 不能为空")
    @Range(min = 1, max = 100, message = "pageSize 必须为 1 到 100 之间的整数")
    private Integer pageSize;
}

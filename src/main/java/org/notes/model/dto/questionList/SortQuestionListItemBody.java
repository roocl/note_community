package org.notes.model.dto.questionList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel("题单项排序请求")
@Data
public class SortQuestionListItemBody {
    @ApiModelProperty(value = "题单ID", required = true)
    @NotNull(message = "questionListId 不能为空")
    @Min(value = 1, message = "questionListId 必须为正整数")
    private Integer questionListId;

    @ApiModelProperty(value = "题目ID排序列表", required = true)
    @NotNull(message = "questionListItemIds 不能为空")
    private List<Integer> questionIds;
}
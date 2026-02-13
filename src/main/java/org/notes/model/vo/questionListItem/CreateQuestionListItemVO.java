package org.notes.model.vo.questionListItem;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("创建题单项结果")
@Data
public class CreateQuestionListItemVO {
    @ApiModelProperty("排序序号")
    private Integer rank;
}

package org.notes.model.vo.questionListItem;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.notes.model.vo.question.BaseQuestionVO;

@ApiModel("题单项VO")
@Data
public class QuestionListItemVO {
    @ApiModelProperty("题单ID")
    private Integer questionListId;

    @ApiModelProperty("题目信息")
    private BaseQuestionVO question;

    @ApiModelProperty("排序序号")
    private Integer rank;
}
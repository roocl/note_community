package org.notes.model.vo.questionListItem;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.notes.model.vo.question.BaseQuestionVO;

@ApiModel("题单项用户VO")
@Data
public class QuestionListItemUserVO {
    @ApiModelProperty("题单ID")
    private Integer questionListId;

    @ApiModelProperty("题目信息")
    private BaseQuestionVO question;

    @ApiModelProperty("用户完成状态")
    private UserQuestionStatus userQuestionStatus;

    @ApiModelProperty("排序序号")
    private Integer rank;

    @ApiModel("题单项用户完成状态")
    @Data
    public static class UserQuestionStatus {
        @ApiModelProperty("是否已完成")
        private boolean finished;
    }
}

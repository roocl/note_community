package org.notes.model.vo.question;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("题目用户VO")
@Data
public class QuestionUserVO {
    @ApiModelProperty("问题ID")
    private Integer questionId;

    @ApiModelProperty("问题标题")
    private String title;

    @ApiModelProperty("问题难度: 1=简单, 2=中等, 3=困难")
    private Integer difficulty;

    @ApiModelProperty("题目考点")
    private String examPoint;

    @ApiModelProperty("浏览量")
    private Integer viewCount;

    @ApiModelProperty("用户完成状态")
    private UserQuestionStatus userQuestionStatus;

    @ApiModel("用户题目完成状态")
    @Data
    public static class UserQuestionStatus {
        @ApiModelProperty("是否已完成")
        private boolean finished = false;
    }
}

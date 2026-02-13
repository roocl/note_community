package org.notes.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel("题单项")
@Data
public class QuestionListItem {
    @ApiModelProperty("题单ID")
    private Integer questionListId;

    @ApiModelProperty("题目ID")
    private Integer questionId;

    @ApiModelProperty("题目排序序号")
    private Integer rank;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    private Date updatedAt;
}

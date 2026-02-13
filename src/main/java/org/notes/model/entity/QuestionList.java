package org.notes.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel("题单")
@Data
public class QuestionList {
    @ApiModelProperty("题单ID")
    private Integer questionListId;

    @ApiModelProperty("题单名称")
    private String name;

    @ApiModelProperty("题单类型")
    private Integer type;

    @ApiModelProperty("题单描述")
    private String description;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    private Date updatedAt;
}
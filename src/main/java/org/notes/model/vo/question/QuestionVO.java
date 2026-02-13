package org.notes.model.vo.question;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel("题目详情VO")
@Data
public class QuestionVO {
    @ApiModelProperty("问题ID")
    private Integer questionId;

    @ApiModelProperty("问题所属分类ID")
    private Integer categoryId;

    @ApiModelProperty("问题标题")
    private String title;

    @ApiModelProperty("问题难度: 1=简单, 2=中等, 3=困难")
    private Integer difficulty;

    @ApiModelProperty("题目考点")
    private String examPoint;

    @ApiModelProperty("浏览量")
    private Integer viewCount;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    private Date updatedAt;
}

package org.notes.model.dto.question;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ApiModel("创建题目请求")
@Data
public class CreateQuestionBody {
    @ApiModelProperty(value = "分类ID", required = true)
    @NotNull(message = "categoryId 不能为空")
    @Min(value = 1, message = "categoryId 必须为正整数")
    private Integer categoryId;

    @ApiModelProperty(value = "题目标题", required = true)
    @NotNull(message = "title 不能为空")
    @NotBlank(message = "title 不能为空")
    @Length(max = 255, message = "title 长度不能超过 255")
    private String title;

    @ApiModelProperty(value = "难度: 1=简单, 2=中等, 3=困难", required = true)
    @NotNull(message = "difficulty 不能为空")
    @Range(min = 1, max = 3, message = "difficulty 必须为 1, 2, 3")
    private Integer difficulty;

    @ApiModelProperty("题目考点")
    @Length(max = 255, message = "examPoint 长度不能超过 255")
    private String examPoint;
}

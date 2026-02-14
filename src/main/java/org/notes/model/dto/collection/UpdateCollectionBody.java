package org.notes.model.dto.collection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;

@ApiModel("修改收藏夹请求")
@Data
public class UpdateCollectionBody {
    @ApiModelProperty(value = "收藏夹名称")
    @Length(max = 32, message = "name 长度不能超过 32")
    private String name;


    @ApiModelProperty(value = "笔记内容")
    @Length(max = 32, message = "name 长度不能超过 255")
    private String description;
}
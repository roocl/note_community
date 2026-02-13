package org.notes.model.vo.upload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("图片上传结果")
@Data
public class ImageVO {
    @ApiModelProperty("图片URL")
    private String url;
}
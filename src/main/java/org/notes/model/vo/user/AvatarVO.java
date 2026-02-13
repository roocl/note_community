package org.notes.model.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("头像上传结果")
@Data
public class AvatarVO {
    @ApiModelProperty("头像URL")
    private String url;
}

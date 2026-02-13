package org.notes.model.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("用户操作状态VO")
@Data
public class UserActionVO {
    @ApiModelProperty("是否已点赞")
    private Boolean isLiked;
}
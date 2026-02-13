package org.notes.model.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("注册结果VO")
@Data
public class RegisterVO {
    @ApiModelProperty("新注册用户ID")
    private Long userId;
}

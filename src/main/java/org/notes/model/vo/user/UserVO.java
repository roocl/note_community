package org.notes.model.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@ApiModel("用户信息VO")
@Data
public class UserVO {
    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("性别: 1=男, 2=女, 3=保密")
    private Integer gender;

    @ApiModelProperty("头像URL")
    private String avatarUrl;

    @ApiModelProperty("用户邮箱")
    private String email;

    @ApiModelProperty("用户学校")
    private String school;

    @ApiModelProperty("个性签名")
    private String signature;

    @ApiModelProperty("最后登录时间")
    private LocalDateTime lastLoginAt;
}

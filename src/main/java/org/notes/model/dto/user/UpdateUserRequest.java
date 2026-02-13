package org.notes.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@ApiModel("更新用户信息请求")
@Data
public class UpdateUserRequest {

    @ApiModelProperty("用户名")
    @Size(min = 1, max = 16, message = "用户名长度必须在1到16个字符之间")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5_a-zA-Z0-9]+$", message = "用户名只能包含中文、字母、数字和下划线")
    private String username;

    @ApiModelProperty("性别: 1=男, 2=女, 3=保密")
    @Min(value = 1, message = "性别取值无效")
    @Max(value = 3, message = "性别取值无效")
    private Integer gender;

    @ApiModelProperty("用户生日")
    @Past(message = "生日必须是一个过去的日期")
    private LocalDate birthday;

    @ApiModelProperty("头像URL")
    @Pattern(regexp = "^(https?|ftp)://.*$", message = "头像地址必须是有效的URL")
    private String avatarUrl;

    @ApiModelProperty("用户邮箱")
    @Email(message = "邮箱格式无效")
    private String email;

    @ApiModelProperty("学校名称")
    @Size(max = 64, message = "学校名称长度不能超过64个字符")
    private String school;

    @ApiModelProperty("个性签名")
    @Size(max = 128, message = "签名长度不能超过128个字符")
    private String signature;
}

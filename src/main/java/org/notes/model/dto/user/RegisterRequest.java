package org.notes.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@ApiModel("注册请求")
@Data
public class RegisterRequest {

    @ApiModelProperty(value = "用户账号", required = true)
    @NotBlank(message = "用户账号不能为空")
    @Size(min = 6, max = 32, message = "账号长度必须在6到32个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "账号只能包含字母、数字和下划线")
    private String account;

    @ApiModelProperty(value = "用户名", required = true)
    @NotBlank(message = "用户名不能为空")
    @Size(max = 16, message = "用户名长度不能超过16个字符")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5_a-zA-Z0-9\\-\\.]+$", message = "用户名只能包含中文、字母、数字、下划线、分隔符")
    private String username;

    @ApiModelProperty(value = "登录密码", required = true)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6到32个字符之间")
    private String password;

    @ApiModelProperty("用户邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @ApiModelProperty("邮箱验证码")
    @Size(min = 6, max = 6, message = "验证码长度必须为6位")
    private String verifyCode;
}

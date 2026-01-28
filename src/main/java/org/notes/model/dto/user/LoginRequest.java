package org.notes.model.dto.user;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class LoginRequest {

    @Size(min = 6, max = 32, message = "账号长度必须在6到32个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "账号只能包含字母、数字和下划线")
    private String account;

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6到32个字符之间")
    private String password;

    @AssertTrue(message = "账号和邮箱必须至少提供一个")
    private boolean isValidLogin() {
        return account != null || email != null;
    }
}

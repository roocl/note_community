package org.notes.model.dto.user;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class UpdateUserRequest {

    @Size(min = 1, max = 16, message = "用户名长度必须在1到16个字符之间")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5_a-zA-Z0-9]+$", message = "用户名只能包含中文、字母、数字和下划线")
    private String username;

    @Min(value = 1, message = "性别取值无效")
    @Max(value = 3, message = "性别取值无效")
    private Integer gender;

    @Past(message = "生日必须是一个过去的日期")
    private LocalDate birthday;

    @Pattern(regexp = "^(https?|ftp)://.*$", message = "头像地址必须是有效的URL")
    private String avatarUrl;

    @Email(message = "邮箱格式无效")
    private String email;

    @Size(max = 64, message = "学校名称长度不能超过64个字符")
    private String school;

    @Size(max = 128, message = "签名长度不能超过128个字符")
    private String signature;
}

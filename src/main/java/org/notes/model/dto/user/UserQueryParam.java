package org.notes.model.dto.user;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class UserQueryParam {

    @Min(value = 1, message = "userId必须为正整数")
    private Long userId;

    private String account;

    @Length(max = 16, message = "用户名长度不能超过16个字符")
    private String username;

    @Min(value = 0, message = "isAdmin最小只能是0")
    @Max(value = 1, message = "isAdmin最大只能是1")
    private Integer isAdmin;

    @Min(value = 0, message = "isBanned最小只能是0")
    @Max(value = 1, message = "isBanned最大只能是1")
    private Integer isBanned;

    @NotNull(message = "page不能为空")
    @Min(value = 1, message = "page必须为正整数")
    private Integer page;

    @NotNull(message = "pageSize不能为空")
    @Min(value = 1, message = "pageSize必须为正整数")
    @Max(value = 200, message = "pageSize不能超过200")
    private Integer pageSize;
}

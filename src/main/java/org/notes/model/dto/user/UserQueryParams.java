package org.notes.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ApiModel("用户查询参数")
@Data
public class UserQueryParams {

    @ApiModelProperty("用户ID")
    @Min(value = 1, message = "userId必须为正整数")
    private Long userId;

    @ApiModelProperty("用户账号")
    private String account;

    @ApiModelProperty("用户名")
    @Length(max = 16, message = "用户名长度不能超过16个字符")
    private String username;

    @ApiModelProperty("是否管理员: 0=否, 1=是")
    @Min(value = 0, message = "isAdmin最小只能是0")
    @Max(value = 1, message = "isAdmin最大只能是1")
    private Integer isAdmin;

    @ApiModelProperty("是否封禁: 0=否, 1=是")
    @Min(value = 0, message = "isBanned最小只能是0")
    @Max(value = 1, message = "isBanned最大只能是1")
    private Integer isBanned;

    @ApiModelProperty(value = "页码", required = true, example = "1")
    @NotNull(message = "page不能为空")
    @Min(value = 1, message = "page必须为正整数")
    private Integer page;

    @ApiModelProperty(value = "每页大小", required = true, example = "20")
    @NotNull(message = "pageSize不能为空")
    @Min(value = 1, message = "pageSize必须为正整数")
    @Max(value = 200, message = "pageSize不能超过200")
    private Integer pageSize;
}

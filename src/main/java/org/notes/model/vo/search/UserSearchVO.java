package org.notes.model.vo.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户搜索结果 VO（含高亮）
 */
@ApiModel("用户搜索结果")
@Data
public class UserSearchVO {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名（可能包含高亮标签）")
    private String username;

    @ApiModelProperty("用户账号")
    private String account;

    @ApiModelProperty("用户邮箱")
    private String email;

    @ApiModelProperty("用户头像地址")
    private String avatarUrl;

    @ApiModelProperty("用户学校（可能包含高亮标签）")
    private String school;

    @ApiModelProperty("用户签名（可能包含高亮标签）")
    private String signature;
}

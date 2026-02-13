package org.notes.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户表
 * 
 * @TableName user
 */
@ApiModel("用户")
@Data
public class User {
    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户账号")
    private String account;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty(value = "加密后的登录密码", hidden = true)
    private String password;

    @ApiModelProperty("性别: 1=男, 2=女, 3=保密")
    private Integer gender;

    @ApiModelProperty("用户生日")
    private LocalDate birthday;

    @ApiModelProperty("用户头像地址")
    private String avatarUrl;

    @ApiModelProperty("用户邮箱")
    private String email;

    @ApiModelProperty("用户学校")
    private String school;

    @ApiModelProperty("用户签名")
    private String signature;

    @ApiModelProperty("封禁状态: 0=未封禁, 1=已封禁")
    private Integer isBanned;

    @ApiModelProperty("管理员状态: 0=普通用户, 1=管理员")
    private Integer isAdmin;

    @ApiModelProperty("最后登录时间")
    private LocalDateTime lastLoginAt;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}
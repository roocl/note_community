package org.notes.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户表
 * @TableName user
 */
@Data
public class User {
    /**
     * 
     */
    private Long userId;

    /**
     * 用户账号,注册时自定义,注册后不可修改,包含数字、字母、下划线
     */
    private String account;

    /**
     * 用户名,可修改,包含中文,字母,数字,下划线
     */
    private String username;

    /**
     * 加密后的登录密码
     */
    private String password;

    /**
     * 性别: 1=男, 2=女, 3=保密
     */
    private Integer gender;

    /**
     * 用户生日
     */
    private LocalDate birthday;

    /**
     * 用户头像地址
     */
    private String avatarUrl;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户学校
     */
    private String school;

    /**
     * 用户签名
     */
    private String signature;

    /**
     * 封禁状态: 0=未封禁, 1=已封禁
     */
    private Integer isBanned;

    /**
     * 管理员状态: 0=普通用户, 1=管理员
     */
    private Integer isAdmin;

    /**
     * 用户最后一次登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 记录创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 记录更新时间
     */
    private LocalDateTime updatedAt;
}
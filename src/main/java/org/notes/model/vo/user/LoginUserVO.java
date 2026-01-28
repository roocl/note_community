package org.notes.model.vo.user;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LoginUserVO {
    private Long userId;

    private String account;

    private String username;

    private Integer gender;

    private LocalDate birthday;

    private String avatarUrl;

    private String email;

    private String school;

    private String signature;

    private Integer isAdmin;
}

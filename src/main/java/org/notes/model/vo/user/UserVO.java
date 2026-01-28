package org.notes.model.vo.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private String username;

    private Integer gender;

    private String avatarUrl;

    private String email;

    private String school;

    private String signature;

    private LocalDateTime lastLoginAt;
}

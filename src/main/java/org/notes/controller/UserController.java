package org.notes.controller;

import lombok.extern.slf4j.Slf4j;
import org.notes.model.base.ApiResponse;
import org.notes.model.dto.user.LoginRequest;
import org.notes.model.dto.user.RegisterRequest;
import org.notes.model.dto.user.UpdateUserRequest;
import org.notes.model.dto.user.UserQueryParams;
import org.notes.model.entity.User;
import org.notes.model.vo.user.AvatarVO;
import org.notes.model.vo.user.LoginUserVO;
import org.notes.model.vo.user.RegisterVO;
import org.notes.model.vo.user.UserVO;
import org.notes.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/users")
    public ApiResponse<RegisterVO> register(
            @Valid
            @RequestBody
            RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/users/login")
    public ApiResponse<LoginUserVO> login(
            @Valid
            @RequestBody
            LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/users/whoami")
    public ApiResponse<LoginUserVO> whoami() {
        return userService.whoami();
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<UserVO> getUserInfo(
            @PathVariable
            @Pattern(regexp = "\\d+", message = "ID格式错误")
            Long userId) {
                return userService.getUserInfo(userId);
    }

    @PatchMapping("/users/me")
    public ApiResponse<LoginUserVO> updateUserInfo(
        @Valid
        @RequestBody
        UpdateUserRequest request) {
        return userService.updateUserInfo(request);
    }

    @PostMapping("/users/avatar")
    public ApiResponse<AvatarVO> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(file);
    }

    @GetMapping("/admin/users")
    public ApiResponse<List<User>> adminGetUser(
            @Valid UserQueryParams queryParam) {
        return userService.getUserList(queryParam);
    }


}

package org.notes.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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

@Api(tags = "用户管理")
@Slf4j
@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;

    @ApiOperation("用户注册")
    @PostMapping("/users")
    public ApiResponse<RegisterVO> register(
            @Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @ApiOperation("用户登录")
    @PostMapping("/users/login")
    public ApiResponse<LoginUserVO> login(
            @Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @ApiOperation("获取当前登录用户信息")
    @PostMapping("/users/whoami")
    public ApiResponse<LoginUserVO> whoami() {
        return userService.whoami();
    }

    @ApiOperation("获取用户信息")
    @GetMapping("/user/{userId}")
    public ApiResponse<UserVO> getUserInfo(
            @ApiParam("用户ID") @PathVariable @Pattern(regexp = "\\d+", message = "ID格式错误") Long userId) {
        return userService.getUserInfo(userId);
    }

    @ApiOperation("更新当前用户信息")
    @PatchMapping("/users/me")
    public ApiResponse<LoginUserVO> updateUserInfo(
            @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUserInfo(request);
    }

    @ApiOperation("上传用户头像")
    @PostMapping("/users/avatar")
    public ApiResponse<AvatarVO> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(file);
    }

    @ApiOperation("获取用户列表（管理端）")
    @GetMapping("/admin/users")
    public ApiResponse<List<User>> adminGetUser(
            @Valid UserQueryParams queryParam) {
        return userService.getUserList(queryParam);
    }

}

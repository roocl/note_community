package org.notes.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.AuthResult;
import org.notes.model.base.PageResult;
import org.notes.model.base.TokenApiResponse;
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
import org.notes.utils.ApiResponseUtil;
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
    public TokenApiResponse<RegisterVO> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResult<RegisterVO> result = userService.register(request);
        return ApiResponseUtil.success("注册成功", result.getData(), result.getToken());
    }

    @ApiOperation("用户登录")
    @PostMapping("/users/login")
    public TokenApiResponse<LoginUserVO> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResult<LoginUserVO> result = userService.login(request);
        return ApiResponseUtil.success("登录成功", result.getData(), result.getToken());
    }

    @ApiOperation("获取当前登录用户信息")
    @PostMapping("/users/whoami")
    public TokenApiResponse<LoginUserVO> whoami() {
        AuthResult<LoginUserVO> result = userService.whoami();
        return ApiResponseUtil.success("自动登录成功", result.getData(), result.getToken());
    }

    @ApiOperation("获取用户信息")
    @GetMapping("/user/{userId}")
    public ApiResponse<UserVO> getUserInfo(
            @ApiParam("用户ID") @PathVariable @Pattern(regexp = "\\d+", message = "ID格式错误") Long userId) {
        return ApiResponseUtil.success("获取用户信息成功", userService.getUserInfo(userId));
    }

    @ApiOperation("更新当前用户信息")
    @PatchMapping("/users/me")
    public ApiResponse<LoginUserVO> updateUserInfo(
            @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponseUtil.success("更新用户信息成功", userService.updateUserInfo(request));
    }

    @ApiOperation("上传用户头像")
    @PostMapping("/users/avatar")
    public ApiResponse<AvatarVO> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        return ApiResponseUtil.success("上传头像成功", userService.uploadAvatar(file));
    }

    @ApiOperation("获取用户列表（管理端）")
    @GetMapping("/admin/users")
    public ApiResponse<List<User>> adminGetUser(
            @Valid UserQueryParams queryParam) {
        PageResult<List<User>> result = userService.getUserList(queryParam);
        return ApiResponseUtil.success("获取用户列表成功", result.getData(), result.getPagination());
    }

}

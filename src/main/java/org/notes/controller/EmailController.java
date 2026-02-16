package org.notes.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.notes.model.base.ApiResponse;
import org.notes.service.EmailService;
import org.notes.utils.ApiResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Api(tags = "邮件服务")
@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @ApiOperation("发送邮箱验证码")
    @GetMapping("/verify-code")
    public ApiResponse<Void> sendVerifyCode(
            @ApiParam("邮箱地址") @RequestParam @NotBlank @Email String email) {
        emailService.sendVerificationCode(email);
        return ApiResponseUtil.success("发送成功", null);
    }
}

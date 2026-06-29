package org.notes.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.notes.annotation.NeedAdmin;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.entity.DlxMessage;
import org.notes.service.DlxMessageService;
import org.notes.utils.ApiResponseUtil;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "RabbitMQ 死信补偿")
@RestController
@RequestMapping("/api/admin/dlx-messages")
@RequiredArgsConstructor
public class DlxMessageController {

    private final DlxMessageService dlxMessageService;

    @ApiOperation("查询死信消息列表")
    @GetMapping
    @NeedAdmin
    public ApiResponse<List<DlxMessage>> listMessages() {
        return ApiResponseUtil.success("查询死信消息成功", dlxMessageService.listMessages());
    }

    @ApiOperation("查询死信消息详情")
    @GetMapping("/{id}")
    @NeedAdmin
    public ApiResponse<DlxMessage> getMessage(@PathVariable Long id) {
        return ApiResponseUtil.success("查询死信消息详情成功", dlxMessageService.getMessage(id));
    }

    @ApiOperation("重新投递死信消息")
    @PostMapping("/{id}/retry")
    @NeedAdmin
    public ApiResponse<EmptyVO> retryMessage(@PathVariable Long id) {
        dlxMessageService.retryMessage(id);
        return ApiResponseUtil.success("死信消息已重新投递");
    }

    @ApiOperation("删除死信消息")
    @DeleteMapping("/{id}")
    @NeedAdmin
    public ApiResponse<EmptyVO> deleteMessage(@PathVariable Long id) {
        dlxMessageService.deleteMessage(id);
        return ApiResponseUtil.success("死信消息已删除");
    }
}

package org.notes.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.notes.annotation.NeedLogin;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.message.ReadMessageBatchRequest;
import org.notes.model.vo.message.MessageVO;
import org.notes.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "消息管理")
@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @ApiOperation("获取消息列表")
    @GetMapping("/message")
    @NeedLogin
    public ApiResponse<List<MessageVO>> getMessages() {
        return messageService.getMessages();
    }

    @ApiOperation("标记单条消息已读")
    @PatchMapping("/{messageId}/read")
    public ApiResponse<EmptyVO> markAsRead(
            @ApiParam("消息ID") @PathVariable Integer messageId) {
        return messageService.markAsRead(messageId);
    }

    @ApiOperation("标记所有消息已读")
    @PatchMapping("/all/read")
    public ApiResponse<EmptyVO> markAllAsRead() {
        return messageService.markAllAsRead();
    }

    @ApiOperation("批量标记消息已读")
    @PatchMapping("/batch/read")
    public ApiResponse<EmptyVO> markAsReadBatch(@RequestBody ReadMessageBatchRequest request) {
        return messageService.markBatchAsRead(request.getMessageIds());
    }

    @ApiOperation("删除消息")
    @DeleteMapping("/{messageId}")
    public ApiResponse<EmptyVO> deleteMessage(
            @ApiParam("消息ID") @PathVariable Integer messageId) {
        return messageService.deleteMessage(messageId);
    }

    @ApiOperation("获取未读消息数量")
    @GetMapping("/unread/count")
    public ApiResponse<Integer> getUnreadCount() {
        return messageService.getUnreadCount();
    }
}

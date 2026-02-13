package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import org.notes.annotation.NeedLogin;
import org.notes.mapper.MessageMapper;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.message.MessageDTO;
import org.notes.model.entity.Message;
import org.notes.model.entity.User;
import org.notes.model.enums.message.MessageType;
import org.notes.model.vo.message.MessageVO;
import org.notes.scope.RequestScopeData;
import org.notes.service.MessageService;
import org.notes.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    private final UserService userService;

    private final RequestScopeData requestScopeData;

    @Override
    public Integer createMessage(MessageDTO messageDTO) {
        try {
            Message message = new Message();
            BeanUtils.copyProperties(messageDTO, message);
            if (messageDTO.getContent() == null) {
                message.setContent("");
            }
            return messageMapper.insert(message);
        } catch (Exception e) {
            throw new RuntimeException("创建消息通知失败: " + e.getMessage());
        }
    }

    @Override
    @NeedLogin
    public ApiResponse<List<MessageVO>> getMessages() {
        Long userId = requestScopeData.getUserId();
        List<Message> messages = messageMapper.selectByUserId(userId);

        List<Long> senders = messages.stream().map(Message::getSenderId).toList();
        Map<Long, User> sendersInfo =  userService.getUserMapByIds(senders);

        List<MessageVO> messageVOS = messages.stream().map(message -> {
            MessageVO messageVO = new MessageVO();
            BeanUtils.copyProperties(message, messageVO);

            MessageVO.Sender sender = new MessageVO.Sender();
            Long senderId = message.getSenderId();
            sender.setUserId(senderId);
            sender.setUsername(sendersInfo.get(senderId).getUsername());
            sender.setAvatarUrl(sendersInfo.get(senderId).getAvatarUrl());
            messageVO.setSender(sender);

            if (!Objects.equals(message.getType(), MessageType.SYSTEM)) {
                MessageVO.Target target = new MessageVO.Target();
                target.setTargetId(message.getTargetId());
                target.setTargetType(message.getTargetType());
                //todo 获取questionSummary
                messageVO.setTarget(target);
            }

            return messageVO;
        }).toList();

        return ApiResponse.success(messageVOS);
    }

    @Override
    @NeedLogin
    public ApiResponse<EmptyVO> markAsRead(Integer messageId) {
        Long userId = requestScopeData.getUserId();
        messageMapper.markAsRead(messageId, userId);
        return ApiResponse.success();
    }

    @Override
    @NeedLogin
    public ApiResponse<EmptyVO> markBatchAsRead(List<Integer> messageIds) {
        Long userId = requestScopeData.getUserId();
        messageMapper.markBatchAsRead(messageIds, userId);
        return ApiResponse.success();
    }

    @Override
    @NeedLogin
    public ApiResponse<EmptyVO> markAllAsRead() {
        Long userId = requestScopeData.getUserId();
        messageMapper.markAllAsRead(userId);
        return ApiResponse.success();
    }

    @Override
    @NeedLogin
    public ApiResponse<EmptyVO> deleteMessage(Integer messageId) {
        Long userId = requestScopeData.getUserId();
        messageMapper.deleteMessage(messageId, userId);
        return ApiResponse.success();
    }

    @Override
    @NeedLogin
    public ApiResponse<Integer> getUnreadCount() {
        Long userId = requestScopeData.getUserId();
        Integer count = messageMapper.countUnread(userId);
        return ApiResponse.success(count);
    }
}

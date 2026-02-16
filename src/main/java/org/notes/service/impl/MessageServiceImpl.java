package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import org.notes.annotation.NeedLogin;
import org.notes.exception.BaseException;
import org.notes.mapper.CommentMapper;
import org.notes.mapper.MessageMapper;
import org.notes.mapper.NoteMapper;
import org.notes.mapper.QuestionMapper;
import org.notes.model.dto.message.MessageDTO;
import org.notes.model.entity.*;
import org.notes.model.enums.message.MessageTargetType;
import org.notes.model.enums.message.MessageType;
import org.notes.model.vo.message.MessageVO;
import org.notes.scope.RequestScopeData;
import org.notes.service.MessageService;
import org.notes.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    private final UserService userService;

    private final RequestScopeData requestScopeData;

    private final NoteMapper noteMapper;

    private final QuestionMapper questionMapper;

    private final CommentMapper commentMapper;

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
            throw new BaseException("创建消息通知失败: " + e.getMessage());
        }
    }

    @Override
    @NeedLogin
    public List<MessageVO> getMessages() {
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
                Integer targetId = message.getTargetId();
                Integer targetType = message.getTargetType();
                target.setTargetId(targetId);
                target.setTargetType(targetType);

                MessageVO.QuestionSummary questionSummary = new MessageVO.QuestionSummary();
                Integer noteId;
                if (Objects.equals(targetType, MessageTargetType.COMMENT)) {
                    Comment comment = commentMapper.findById(targetId);
                    noteId = comment.getNoteId();
                } else {
                    noteId = targetId;
                }

                Note note = noteMapper.findById(noteId);
                Question question = questionMapper.findById(note.getQuestionId());
                questionSummary.setQuestionId(question.getQuestionId());
                questionSummary.setTitle(question.getTitle());

                target.setQuestionSummary(questionSummary);
                messageVO.setTarget(target);
            }

            return messageVO;
        }).toList();

        return messageVOS;
    }

    @Override
    @NeedLogin
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Integer messageId) {
        Long userId = requestScopeData.getUserId();
        messageMapper.markAsRead(messageId, userId);
    }

    @Override
    @NeedLogin
    @Transactional(rollbackFor = Exception.class)
    public void markBatchAsRead(List<Integer> messageIds) {
        Long userId = requestScopeData.getUserId();
        messageMapper.markBatchAsRead(messageIds, userId);
    }

    @Override
    @NeedLogin
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead() {
        Long userId = requestScopeData.getUserId();
        messageMapper.markAllAsRead(userId);
    }

    @Override
    @NeedLogin
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Integer messageId) {
        Long userId = requestScopeData.getUserId();
        messageMapper.deleteMessage(messageId, userId);
    }

    @Override
    @NeedLogin
    public Integer getUnreadCount() {
        Long userId = requestScopeData.getUserId();
        return messageMapper.countUnread(userId);
    }
}

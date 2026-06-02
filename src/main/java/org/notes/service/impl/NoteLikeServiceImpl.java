package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import org.notes.annotation.NeedLogin;
import org.notes.config.RabbitMQConfig;
import org.notes.exception.BaseException;
import org.notes.exception.NotFoundException;
import org.notes.mapper.NoteLikeMapper;
import org.notes.mapper.NoteMapper;
import org.notes.model.entity.Note;
import org.notes.model.entity.NoteLike;
import org.notes.model.enums.message.MessageTargetType;
import org.notes.model.enums.message.MessageType;
import org.notes.scope.RequestScopeData;
import org.notes.service.NoteLikeService;
import org.notes.task.notification.NotificationTask;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NoteLikeServiceImpl implements NoteLikeService {

    private final NoteLikeMapper noteLikeMapper;

    private final NoteMapper noteMapper;

    private final RequestScopeData requestScopeData;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public Set<Integer> findUserLikedNoteIds(Long userId, List<Integer> noteIds) {
        List<Integer> likedIds = noteLikeMapper.findUserLikedNoteIds(userId, noteIds);
        return new HashSet<>(likedIds);
    }

    @Override
    @NeedLogin
    @Transactional(rollbackFor = Exception.class)
    public void likeNote(Integer noteId) {
        Note note = noteMapper.findById(noteId);
        if (note == null) {
            throw new NotFoundException("笔记未找到");
        }

        Long userId = requestScopeData.getUserId();

        try {
            NoteLike noteLike = new NoteLike();
            noteLike.setNoteId(noteId);
            noteLike.setUserId(userId);
            noteLikeMapper.insert(noteLike);

            noteMapper.likeNote(noteId);

            NotificationTask notificationTask = new NotificationTask();
            notificationTask.setReceiverId(note.getAuthorId());
            notificationTask.setSenderId(userId);
            notificationTask.setType(MessageType.LIKE);
            notificationTask.setTargetId(noteId);
            notificationTask.setTargetType(MessageTargetType.NOTE);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_QUEUE, notificationTask);
                }
            });
        } catch (Exception e) {
            throw new BaseException("点赞失败", e);
        }
    }

    @Override
    @NeedLogin
    @Transactional(rollbackFor = Exception.class)
    public void unlikeNote(Integer noteId) {
        Note note = noteMapper.findById(noteId);
        if (note == null) {
            throw new NotFoundException("笔记未找到");
        }

        Long userId = requestScopeData.getUserId();

        try {
            NoteLike noteLike = noteLikeMapper.findByUserIdAndNoteId(userId, noteId);

            if (noteLike != null) {
                noteLikeMapper.delete(noteLike);
                noteMapper.unlikeNote(noteId);
            }
        } catch (Exception e) {
            throw new BaseException("取消点赞失败", e);
        }
    }
}

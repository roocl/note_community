package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import org.notes.annotation.NeedLogin;
import org.notes.mapper.NoteLikeMapper;
import org.notes.mapper.NoteMapper;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.message.MessageDTO;
import org.notes.model.entity.Note;
import org.notes.model.entity.NoteLike;
import org.notes.model.enums.message.MessageTargetType;
import org.notes.model.enums.message.MessageType;
import org.notes.scope.RequestScopeData;
import org.notes.service.MessageService;
import org.notes.service.NoteLikeService;
import org.notes.service.NoteService;
import org.notes.utils.ApiResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NoteLikeServiceImpl implements NoteLikeService {

    private final NoteLikeMapper noteLikeMapper;

    private final NoteMapper noteMapper;

    private final RequestScopeData requestScopeData;

    private final MessageService messageService;

    @Override
    public Set<Integer> findUserLikedNoteIds(Long userId, List<Integer> noteIds) {
        return Set.of();
    }

    @Override
    @NeedLogin
    @Transactional
    public ApiResponse<EmptyVO> likeNote(Integer noteId) {
        Note note = noteMapper.findById(noteId);
        if (note == null) {
            return ApiResponseUtil.error("笔记未找到");
        }

        Long userId = requestScopeData.getUserId();

        try {
            NoteLike noteLike = new NoteLike();
            noteLike.setNoteId(noteId);
            noteLike.setUserId(userId);
            noteLikeMapper.insert(noteLike);

            noteMapper.likeNote(noteId);

            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setReceiverId(note.getAuthorId());
            messageDTO.setSenderId(userId);
            messageDTO.setType(MessageType.LIKE);
            messageDTO.setTargetId(noteId);
            messageDTO.setTargetType(MessageTargetType.NOTE);
            messageDTO.setIsRead(false);

            System.out.println(messageDTO);

            return ApiResponseUtil.success("点赞成功");
        } catch (Exception e) {
            return ApiResponseUtil.error("点赞失败");
        }
    }

    @Override
    @NeedLogin
    @Transactional
    public ApiResponse<EmptyVO> unlikeNote(Integer noteId) {
        Note note = noteMapper.findById(noteId);
        if (note == null) {
            return ApiResponseUtil.error("笔记未找到");
        }

        Long userId = requestScopeData.getUserId();

        try {
            NoteLike noteLike = noteLikeMapper.findByUserIdAndNoteId(userId, noteId);

            if (noteLike != null) {
                noteLikeMapper.delete(noteLike);
                noteMapper.unlikeNote(noteId);
            }

            return ApiResponseUtil.success("取消点赞成功");
        } catch (Exception e) {
            return ApiResponseUtil.error("取消点赞失败");
        }
    }
}

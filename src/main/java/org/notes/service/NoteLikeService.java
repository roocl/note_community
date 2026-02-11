package org.notes.service;

import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional
public interface NoteLikeService {
    Set<Integer> findUserLikedNoteIds(Long userId, List<Integer> noteIds);

    ApiResponse<EmptyVO> likeNote(Integer noteId);

    ApiResponse<EmptyVO> unlikeNote(Integer noteId);
}

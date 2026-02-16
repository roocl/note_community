package org.notes.service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional
public interface NoteLikeService {
    Set<Integer> findUserLikedNoteIds(Long userId, List<Integer> noteIds);

    void likeNote(Integer noteId);

    void unlikeNote(Integer noteId);
}

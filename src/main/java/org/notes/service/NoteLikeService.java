package org.notes.service;

import java.util.List;
import java.util.Set;

public interface NoteLikeService {
    Set<Integer> findUserLikedNoteIds(Long userId, List<Integer> noteIds);

    void likeNote(Integer noteId);

    void unlikeNote(Integer noteId);
}

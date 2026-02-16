package org.notes.service;

import org.notes.model.vo.search.NoteSearchVO;
import org.notes.model.vo.search.UserSearchVO;

import java.util.List;

public interface SearchService {

    List<NoteSearchVO> searchNotes(String keyword, int page, int pageSize);

    List<UserSearchVO> searchUsers(String keyword, int page, int pageSize);
}

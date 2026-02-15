package org.notes.service;

import org.notes.model.base.ApiResponse;
import org.notes.model.vo.search.NoteSearchVO;
import org.notes.model.vo.search.UserSearchVO;

import java.util.List;

public interface SearchService {

    ApiResponse<List<NoteSearchVO>> searchNotes(String keyword, int page, int pageSize);

    ApiResponse<List<UserSearchVO>> searchUsers(String keyword, int page, int pageSize);
}

package org.notes.service;

import org.notes.model.vo.search.NoteSearchResultVO;
import org.notes.model.vo.search.UserSearchVO;

import java.util.List;

public interface SearchService {

    NoteSearchResultVO searchNotes(String keyword, int page, int pageSize);

    List<UserSearchVO> searchUsers(String keyword, int page, int pageSize);

    /**
     * 搜索建议（自动补全），返回建议列表
     */
    List<String> suggest(String keyword);
}

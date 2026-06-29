package org.notes.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.notes.mapper.NoteMapper;
import org.notes.mapper.QuestionListItemMapper;
import org.notes.mapper.QuestionListMapper;
import org.notes.mapper.UserMapper;
import org.notes.model.base.PageResult;
import org.notes.model.dto.questionList.CreateQuestionListItemBody;
import org.notes.model.dto.questionList.SortQuestionListItemBody;
import org.notes.model.dto.questionListItem.QuestionListItemQueryParams;
import org.notes.model.vo.questionListItem.QuestionListItemUserVO;
import org.notes.scope.RequestScopeData;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionListItemServiceImplTest {

    @Mock
    private QuestionListItemMapper questionListItemMapper;
    @Mock
    private QuestionListMapper questionListMapper;
    @Mock
    private RequestScopeData requestScopeData;
    @Mock
    private NoteMapper noteMapper;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private QuestionListItemServiceImpl questionListItemService;

    @Test
    void userGetQuestionListItems_returnsPagedResult() {
        QuestionListItemQueryParams params = new QuestionListItemQueryParams();
        params.setQuestionListId(1);
        params.setPage(1);
        params.setPageSize(10);
        when(questionListItemMapper.countByQuestionListId(1)).thenReturn(0);
        when(questionListItemMapper.findByQuestionListIdPage(1, 10, 0)).thenReturn(Collections.emptyList());
        when(requestScopeData.isLogin()).thenReturn(false);

        PageResult<List<QuestionListItemUserVO>> result = questionListItemService.userGetQuestionListItems(params);

        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getPagination().getTotal());
    }

    @Test
    void createQuestionListItem_assignsNextRank() {
        CreateQuestionListItemBody body = new CreateQuestionListItemBody();
        body.setQuestionListId(1);
        body.setQuestionId(2);
        when(questionListItemMapper.nextRank(1)).thenReturn(3);

        assertEquals(3, questionListItemService.createQuestionListItem(body).getRank());

        verify(questionListItemMapper).insert(any());
    }

    @Test
    void sortQuestionListItem_updatesEveryRank() {
        SortQuestionListItemBody body = new SortQuestionListItemBody();
        body.setQuestionListId(1);
        body.setQuestionIds(List.of(3, 1, 2));

        questionListItemService.sortQuestionListItem(body);

        verify(questionListItemMapper, times(3)).updateQuestionRank(any());
    }
}

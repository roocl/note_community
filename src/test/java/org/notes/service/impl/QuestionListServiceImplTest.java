package org.notes.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.notes.exception.NotFoundException;
import org.notes.mapper.QuestionListItemMapper;
import org.notes.mapper.QuestionListMapper;
import org.notes.model.dto.questionList.CreateQuestionListBody;
import org.notes.model.entity.QuestionList;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionListServiceImplTest {

    @Mock
    private QuestionListMapper questionListMapper;
    @Mock
    private QuestionListItemMapper questionListItemMapper;

    @InjectMocks
    private QuestionListServiceImpl questionListService;

    private QuestionList questionList;

    @BeforeEach
    void setUp() {
        questionList = new QuestionList();
        questionList.setQuestionListId(1);
        questionList.setName("Backend");
    }

    @Test
    void getQuestionList_returnsEntity() {
        when(questionListMapper.findById(1)).thenReturn(questionList);

        assertSame(questionList, questionListService.getQuestionList(1));
    }

    @Test
    void getQuestionList_throwsWhenMissing() {
        when(questionListMapper.findById(99)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> questionListService.getQuestionList(99));
    }

    @Test
    void getQuestionLists_returnsAllRows() {
        when(questionListMapper.findAll()).thenReturn(List.of(questionList));

        assertEquals(1, questionListService.getQuestionLists().size());
    }

    @Test
    void createQuestionList_insertsEntity() {
        CreateQuestionListBody body = new CreateQuestionListBody();
        body.setName("New list");

        assertNotNull(questionListService.createQuestionList(body));

        verify(questionListMapper).insert(any(QuestionList.class));
    }
}

package org.notes.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.notes.exception.NotFoundException;
import org.notes.mapper.CategoryMapper;
import org.notes.mapper.NoteMapper;
import org.notes.mapper.QuestionMapper;
import org.notes.model.base.PageResult;
import org.notes.model.dto.question.CreateQuestionBody;
import org.notes.model.dto.question.QuestionQueryParams;
import org.notes.model.dto.question.SearchQuestionBody;
import org.notes.model.entity.Category;
import org.notes.model.entity.Question;
import org.notes.model.vo.question.QuestionUserVO;
import org.notes.model.vo.question.QuestionVO;
import org.notes.scope.RequestScopeData;
import org.notes.service.CategoryService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @Mock
    private QuestionMapper questionMapper;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private RequestScopeData requestScopeData;
    @Mock
    private NoteMapper noteMapper;
    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private QuestionServiceImpl questionService;

    private Question question;

    @BeforeEach
    void setUp() {
        question = new Question();
        question.setQuestionId(1);
        question.setTitle("What is JVM?");
        question.setCategoryId(1);
    }

    @Test
    void getQuestionMapByIds_returnsMapByQuestionId() {
        when(questionMapper.findByIdBatch(List.of(1))).thenReturn(List.of(question));

        Map<Integer, Question> result = questionService.getQuestionMapByIds(List.of(1));

        assertSame(question, result.get(1));
    }

    @Test
    void userGetQuestion_throwsWhenMissing() {
        when(questionMapper.findById(9)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> questionService.userGetQuestion(9));
    }

    @Test
    void userGetQuestions_returnsPagedResult() {
        QuestionQueryParams params = new QuestionQueryParams();
        params.setPage(1);
        params.setPageSize(10);
        when(questionMapper.countByQueryParam(params)).thenReturn(1);
        when(questionMapper.findByQueryParam(params, 0, 10)).thenReturn(List.of(question));
        when(requestScopeData.isLogin()).thenReturn(false);

        PageResult<List<QuestionUserVO>> result = questionService.userGetQuestions(params);

        assertEquals(1, result.getData().size());
        assertEquals(1, result.getPagination().getTotal());
    }

    @Test
    void searchQuestion_mapsEntitiesToVos() {
        SearchQuestionBody body = new SearchQuestionBody();
        body.setKeyword("JVM");
        when(questionMapper.findByKeyword("JVM")).thenReturn(List.of(question));

        List<QuestionVO> result = questionService.searchQuestion(body);

        assertEquals("What is JVM?", result.get(0).getTitle());
    }

    @Test
    void adminCreateQuestion_requiresExistingCategory() {
        CreateQuestionBody body = new CreateQuestionBody();
        body.setCategoryId(1);
        body.setTitle("Question");
        when(categoryMapper.findById(1)).thenReturn(new Category());

        assertNotNull(questionService.adminCreateQuestion(body));

        verify(questionMapper).insert(any(Question.class));
    }
}

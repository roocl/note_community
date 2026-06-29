package org.notes.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.notes.exception.ForbiddenException;
import org.notes.exception.NotFoundException;
import org.notes.mapper.NoteMapper;
import org.notes.model.base.PageResult;
import org.notes.model.dto.note.NoteQueryParams;
import org.notes.model.dto.note.UpdateNoteRequest;
import org.notes.model.entity.Note;
import org.notes.model.entity.Question;
import org.notes.model.entity.User;
import org.notes.model.vo.note.NoteRankListItem;
import org.notes.model.vo.note.NoteVO;
import org.notes.repository.NoteSearchRepository;
import org.notes.scope.RequestScopeData;
import org.notes.service.CategoryService;
import org.notes.service.CollectionNoteService;
import org.notes.service.EsSyncFailureService;
import org.notes.service.NoteLikeService;
import org.notes.service.QuestionService;
import org.notes.service.RedisProtectionService;
import org.notes.service.UserService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Collections;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private NoteMapper noteMapper;
    @Mock
    private UserService userService;
    @Mock
    private QuestionService questionService;
    @Mock
    private NoteLikeService noteLikeService;
    @Mock
    private CollectionNoteService collectionNoteService;
    @Mock
    private RequestScopeData requestScopeData;
    @Mock
    private CategoryService categoryService;
    @Mock
    private NoteSearchRepository noteSearchRepository;
    @Mock
    private EsSyncFailureService esSyncFailureService;
    @Mock
    private RedisProtectionService redisProtectionService;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private NoteServiceImpl noteService;

    private Note note;

    @BeforeEach
    void setUp() {
        note = new Note();
        note.setNoteId(1);
        note.setAuthorId(1L);
        note.setQuestionId(10);
        note.setContent("content");
    }

    @Test
    void getNotes_returnsPagedVos() {
        NoteQueryParams params = new NoteQueryParams();
        params.setPage(1);
        params.setPageSize(10);
        when(noteMapper.countNotesByQueryParam(params)).thenReturn(1);
        when(noteMapper.findByQueryParams(params, 0, 10)).thenReturn(List.of(note));
        User user = new User();
        user.setUserId(1L);
        user.setUsername("alice");
        Question question = new Question();
        question.setQuestionId(10);
        question.setTitle("Question");
        when(userService.getUserMapByIds(List.of(1L))).thenReturn(Map.of(1L, user));
        when(questionService.getQuestionMapByIds(List.of(10))).thenReturn(Map.of(10, question));
        when(requestScopeData.isLogin()).thenReturn(false);

        PageResult<List<NoteVO>> result = noteService.getNotes(params);

        assertEquals(1, result.getData().size());
        assertEquals(1, result.getPagination().getTotal());
    }

    @Test
    void updateNote_throwsWhenMissing() {
        when(noteMapper.findById(9)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> noteService.updateNote(9, new UpdateNoteRequest()));
    }

    @Test
    void updateNote_throwsWhenCurrentUserIsNotAuthor() {
        when(noteMapper.findById(1)).thenReturn(note);
        when(requestScopeData.getUserId()).thenReturn(2L);

        assertThrows(ForbiddenException.class, () -> noteService.updateNote(1, new UpdateNoteRequest()));
    }

    @Test
    void updateNote_recordsEsSyncFailureWhenSaveFails() {
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setContent("new content");
        when(noteMapper.findById(1)).thenReturn(note);
        when(requestScopeData.getUserId()).thenReturn(1L);
        doThrow(new RuntimeException("es down")).when(noteSearchRepository).save(any());

        noteService.updateNote(1, request);

        verify(esSyncFailureService).recordFailure(
                eq(EsSyncFailureServiceImpl.ENTITY_NOTE),
                eq(1L),
                eq(EsSyncFailureServiceImpl.OP_SAVE),
                any(RuntimeException.class));
    }

    @Test
    void submitNoteRank_fallsBackToMysqlWhenRedisEmpty() {
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(9L))).thenReturn(Collections.emptySet());
        NoteRankListItem item = new NoteRankListItem();
        item.setUserId(1L);
        item.setNoteCount(3);
        when(noteMapper.submitNoteRank()).thenReturn(List.of(item));
        when(redisProtectionService.tryLock(anyString(), eq(Duration.ofSeconds(5)))).thenReturn("token");
        when(redisProtectionService.withJitter(eq(Duration.ofMinutes(30)), eq(300))).thenReturn(Duration.ofMinutes(31));

        assertEquals(1, noteService.submitNoteRank().size());
        verify(zSetOperations).add(anyString(), eq("1"), eq(3.0));
        verify(stringRedisTemplate).expire(anyString(), eq(Duration.ofMinutes(31)));
        verify(redisProtectionService).unlock(anyString(), eq("token"));
    }

    @Test
    void submitNoteRank_doesNotRebuildWhenLockUnavailable() {
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(9L))).thenReturn(Collections.emptySet());
        NoteRankListItem item = new NoteRankListItem();
        when(noteMapper.submitNoteRank()).thenReturn(List.of(item));
        when(redisProtectionService.tryLock(anyString(), eq(Duration.ofSeconds(5)))).thenReturn(null);

        assertEquals(1, noteService.submitNoteRank().size());
        verify(zSetOperations, never()).add(anyString(), anyString(), anyDouble());
        verify(redisProtectionService, never()).unlock(anyString(), anyString());
    }
}

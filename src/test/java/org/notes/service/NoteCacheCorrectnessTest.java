package org.notes.service;

import org.junit.jupiter.api.Test;
import org.notes.mapper.*;
import org.notes.model.dto.note.NoteQueryParams;
import org.notes.model.entity.Note;
import org.notes.model.entity.User;
import org.notes.model.entity.Question;
import org.notes.model.vo.note.NoteVO;
import org.notes.scope.RequestScopeData;
import org.notes.service.impl.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoteCacheCorrectnessTest {
    @Test
    void cachedListKeepsUserActionsIsolated() {
        NoteMapper notes = mock(NoteMapper.class);
        UserMapper users = mock(UserMapper.class);
        QuestionMapper questions = mock(QuestionMapper.class);
        NoteLikeMapper likes = mock(NoteLikeMapper.class);
        CollectionNoteMapper collections = mock(CollectionNoteMapper.class);
        RequestScopeData scope = new RequestScopeData();
        scope.setLogin(true);
        scope.setUserId(1L);
        Note note = new Note();
        note.setNoteId(7);
        note.setAuthorId(3L);
        note.setQuestionId(10);
        when(notes.countNotesByQueryParam(any())).thenReturn(1);
        when(notes.findByQueryParams(any(), eq(0), eq(10))).thenReturn(List.of(note));
        User author = new User();
        author.setUserId(3L);
        author.setUsername("original-author");
        Question question = new Question();
        question.setQuestionId(10);
        question.setTitle("original-title");
        when(users.findByIdBatch(any())).thenReturn(List.of(author));
        when(questions.findByIdBatch(any())).thenReturn(List.of(question));
        when(likes.findUserLikedNoteIds(1L, List.of(7))).thenReturn(List.of(7));
        when(likes.findUserLikedNoteIds(2L, List.of(7))).thenReturn(List.of());
        when(collections.findUserCollectedNoteIds(1L, List.of(7))).thenReturn(List.of());
        when(collections.findUserCollectedNoteIds(2L, List.of(7))).thenReturn(List.of(7));
        UserServiceImpl userService = new UserServiceImpl();
        ReflectionTestUtils.setField(userService, "userMapper", users);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        Map<String, String> stored = new HashMap<>();
        when(values.get(anyString())).thenAnswer(call -> stored.get(call.getArgument(0)));
        when(values.setIfAbsent(anyString(), anyString())).thenAnswer(call -> stored.putIfAbsent(call.getArgument(0), call.getArgument(1)) == null);
        doAnswer(call -> { stored.put(call.getArgument(0), call.getArgument(1)); return null; }).when(values).set(anyString(), anyString());
        doAnswer(call -> { stored.put(call.getArgument(0), call.getArgument(1)); return null; })
                .when(values).set(anyString(), anyString(), any(java.time.Duration.class));
        NoteServiceImpl target = new NoteServiceImpl(notes,
                new NoteListCache(notes, redis, new ObjectMapper().findAndRegisterModules()), userService,
                new QuestionServiceImpl(questions, null, scope, notes, null),
                new NoteLikeServiceImpl(likes, notes, scope, null, null),
                new CollectionNoteServiceImpl(null, collections, notes, scope, null),
                scope, null, null, null, null, null, null);
        NoteService service = target;
        NoteQueryParams query = new NoteQueryParams();
        NoteVO first = service.getNotes(query).getData().get(0);
        scope.setUserId(2L);
        author.setUsername("updated-author");
        question.setTitle("updated-title");
        NoteVO second = service.getNotes(query).getData().get(0);
        assertTrue(first.getUserActionsVO().getIsLiked());
        assertEquals(Boolean.FALSE, second.getUserActionsVO().getIsLiked());
        assertTrue(second.getUserActionsVO().getIsCollected());
        assertEquals("original-author", first.getAuthor().getUsername());
        assertEquals("updated-author", second.getAuthor().getUsername());
        assertEquals("updated-title", second.getQuestion().getTitle());
        assertNotSame(first, second);
        scope.setLogin(false);
        scope.setUserId(null);
        NoteVO anonymous = service.getNotes(query).getData().get(0);
        assertFalse(anonymous.getUserActionsVO().getIsLiked());
        assertFalse(anonymous.getUserActionsVO().getIsCollected());
        assertTrue(first.getUserActionsVO().getIsLiked());
        verify(notes, times(1)).findByQueryParams(any(), eq(0), eq(10));
    }
}

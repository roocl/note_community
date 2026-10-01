package org.notes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.notes.mapper.NoteMapper;
import org.notes.model.dto.note.NoteQueryParams;
import org.notes.model.entity.Note;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoteListCacheTest {
    private final Map<String,String> stored = new ConcurrentHashMap<>();
    private final AtomicReference<Note> current = new AtomicReference<>();
    private NoteMapper mapper;
    private NoteListCache cache;

    @BeforeEach
    void setUp() {
        mapper = mock(NoteMapper.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String,String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(anyString())).thenAnswer(call -> stored.get(call.getArgument(0)));
        when(values.setIfAbsent(anyString(), anyString())).thenAnswer(call -> stored.putIfAbsent(call.getArgument(0), call.getArgument(1)) == null);
        doAnswer(call -> { stored.put(call.getArgument(0), call.getArgument(1)); return null; }).when(values).set(anyString(), anyString());
        doAnswer(call -> { stored.put(call.getArgument(0), call.getArgument(1)); return null; }).when(values).set(anyString(), anyString(), any(Duration.class));
        when(mapper.countNotesByQueryParam(any())).thenReturn(1);
        when(mapper.findByQueryParams(any(), anyInt(), anyInt())).thenAnswer(call -> List.of(current.get()));
        Note old = new Note();
        old.setNoteId(7);
        old.setContent("old");
        current.set(old);
        cache = new NoteListCache(mapper, redis, new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void differentQueriesDoNotShareLegacyHashCollision() {
        NoteQueryParams first = new NoteQueryParams();
        first.setQuestionId(1);
        first.setAuthorId(32L);
        NoteQueryParams second = new NoteQueryParams();
        second.setQuestionId(2);
        second.setAuthorId(1L);
        assertEquals("old", cache.getNotes(first).notes().get(0).getContent());
        Note other = new Note();
        other.setNoteId(8);
        other.setContent("other-query");
        current.set(other);
        assertEquals("other-query", cache.getNotes(second).notes().get(0).getContent());
        assertEquals("old", cache.getNotes(first).notes().get(0).getContent());
    }

    @Test
    void missingGenerationDoesNotReviveOldCachedData() {
        NoteQueryParams query = new NoteQueryParams();
        assertEquals("old", cache.getNotes(query).notes().get(0).getContent());
        Note updated = new Note();
        updated.setNoteId(7);
        updated.setContent("updated");
        current.set(updated);
        stored.remove("note:list:generation");
        assertEquals("updated", cache.getNotes(query).notes().get(0).getContent());
    }

    @Test
    void lateQueryCannotRefillCurrentGenerationWithOldData() throws Exception {
        CountDownLatch queried = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(mapper.findByQueryParams(any(), anyInt(), anyInt())).thenAnswer(call -> {
            Note snapshot = current.get();
            if ("old".equals(snapshot.getContent())) {
                queried.countDown();
                assertTrue(release.await(10, TimeUnit.SECONDS));
            }
            return List.of(snapshot);
        });
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> oldRead = executor.submit(() -> cache.getNotes(new NoteQueryParams()));
            assertTrue(queried.await(10, TimeUnit.SECONDS));
            Note updated = new Note();
            updated.setNoteId(7);
            updated.setContent("updated");
            current.set(updated);
            cache.invalidateAfterCommit();
            assertEquals("updated", cache.getNotes(new NoteQueryParams()).notes().get(0).getContent());
            release.countDown();
            oldRead.get(10, TimeUnit.SECONDS);
            assertEquals("updated", cache.getNotes(new NoteQueryParams()).notes().get(0).getContent());
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }
}

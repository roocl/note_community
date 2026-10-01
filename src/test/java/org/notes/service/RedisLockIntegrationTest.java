package org.notes.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.notes.service.impl.RedisProtectionServiceImpl;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;
import java.util.UUID;
import java.time.Duration;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.notes.mapper.NoteMapper;
import org.notes.model.entity.Note;
import org.notes.model.dto.note.NoteQueryParams;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnabledIfSystemProperty(named = "integration.real", matches = "true")
class RedisLockIntegrationTest {
    private LettuceConnectionFactory factory;
    private StringRedisTemplate actual;
    private String key;

    @BeforeEach
    void setUp() {
        factory = new LettuceConnectionFactory(System.getProperty("integration.redis.host", "localhost"),
                Integer.getInteger("integration.redis.port", 16379));
        factory.setDatabase(14);
        factory.afterPropertiesSet();
        actual = new StringRedisTemplate(factory);
        key = "test:lock:" + UUID.randomUUID();
    }

    @AfterEach
    void cleanUp() {
        actual.delete(key);
        java.util.Set<String> keys = actual.keys("note:list:*");
        if (keys != null && !keys.isEmpty()) {
            actual.delete(keys);
        }
        factory.destroy();
    }

    @Test
    void ownerCanReleaseLockAndOtherOwnerCannot() {
        RedisProtectionService service = new RedisProtectionServiceImpl(actual);
        String token = service.tryLock(key, Duration.ofSeconds(10));
        assertNotNull(token);
        assertNull(service.tryLock(key, Duration.ofSeconds(10)));
        service.unlock(key, "different-owner");
        assertEquals(token, actual.opsForValue().get(key));
        service.unlock(key, token);
        assertNull(actual.opsForValue().get(key));
    }

    @Test
    void cachedNotesRoundTripAndBecomeFreshAfterInvalidation() {
        NoteMapper mapper = mock(NoteMapper.class);
        Note note = new Note();
        note.setNoteId(7);
        note.setContent("old");
        note.setCreatedAt(LocalDateTime.of(2026, 10, 1, 12, 0));
        when(mapper.countNotesByQueryParam(any())).thenReturn(1);
        when(mapper.findByQueryParams(any(), anyInt(), anyInt())).thenReturn(List.of(note));
        NoteListCache cache = new NoteListCache(mapper, actual, new ObjectMapper().findAndRegisterModules());
        assertEquals("old", cache.getNotes(new NoteQueryParams()).notes().get(0).getContent());
        assertEquals(note.getCreatedAt(), cache.getNotes(new NoteQueryParams()).notes().get(0).getCreatedAt());
        verify(mapper, times(1)).findByQueryParams(any(), anyInt(), anyInt());
        note.setContent("updated");
        cache.invalidateAfterCommit();
        assertEquals("updated", cache.getNotes(new NoteQueryParams()).notes().get(0).getContent());
    }

    @Test
    void oldOwnerCannotDeleteReplacementLock() {
        actual.opsForValue().set(key, "old-owner");
        ValueOperations<String,String> interleaved = spy(actual.opsForValue());
        doAnswer(call -> {
            String old = actual.opsForValue().get(key);
            actual.opsForValue().set(key, "new-owner");
            return old;
        }).when(interleaved).get(key);
        StringRedisTemplate contended = new StringRedisTemplate(factory) {
            @Override
            public ValueOperations<String,String> opsForValue() {
                return interleaved;
            }
            @Override
            public <T> T execute(RedisScript<T> script, List<String> keys, Object... args) {
                actual.opsForValue().set(key, "new-owner");
                return super.execute(script, keys, args);
            }
        };
        new RedisProtectionServiceImpl(contended).unlock(key, "old-owner");
        assertEquals("new-owner", actual.opsForValue().get(key));
    }
}

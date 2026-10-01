package org.notes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.notes.mapper.NoteMapper;
import org.notes.model.base.Pagination;
import org.notes.model.dto.note.NoteQueryParams;
import org.notes.model.entity.Note;
import org.notes.utils.PaginationUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteListCache {
    private static final String GENERATION_KEY = "note:list:generation";
    private final NoteMapper noteMapper;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public Snapshot getNotes(NoteQueryParams params) {
        String key = null;
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            try {
                String generation = redis.opsForValue().get(GENERATION_KEY);
                if (generation == null) {
                    String candidate = UUID.randomUUID().toString();
                    generation = Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(GENERATION_KEY, candidate))
                            ? candidate : redis.opsForValue().get(GENERATION_KEY);
                    if (generation == null) {
                        throw new IllegalStateException("缓存代次不可用");
                    }
                }
                key = "note:list:" + generation + ":"
                        + LocalDate.now() + ":" + objectMapper.writeValueAsString(params);
                String cached = redis.opsForValue().get(key);
                if (cached != null) {
                    return objectMapper.readValue(cached, Snapshot.class);
                }
            } catch (Exception e) {
                log.warn("笔记列表缓存读取失败", e);
                key = null;
            }
        }
        int total = noteMapper.countNotesByQueryParam(params);
        List<Note> notes = noteMapper.findByQueryParams(params,
                PaginationUtils.calculateOffset(params.getPage(), params.getPageSize()), params.getPageSize());
        Snapshot snapshot = new Snapshot(notes, new Pagination(params.getPage(), params.getPageSize(), total));
        if (key != null) {
            try {
                redis.opsForValue().set(key, objectMapper.writeValueAsString(snapshot), Duration.ofMinutes(30));
            } catch (Exception e) {
                log.warn("笔记列表缓存写入失败", e);
            }
        }
        return snapshot;
    }

    public void invalidateAfterCommit() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    invalidate();
                }
            });
        } else {
            invalidate();
        }
    }

    private void invalidate() {
        try {
            redis.opsForValue().set(GENERATION_KEY, UUID.randomUUID().toString());
        } catch (Exception e) {
            log.warn("笔记列表缓存失效失败", e);
        }
    }

    public record Snapshot(List<Note> notes, Pagination pagination) {}
}

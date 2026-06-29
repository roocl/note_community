package org.notes.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.notes.mapper.CollectionNoteMapper;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CollectionNoteServiceImpl 单元测试")
class CollectionNoteServiceImplTest {

    @Mock
    private CollectionNoteMapper collectionNoteMapper;

    @InjectMocks
    private CollectionNoteServiceImpl collectionNoteService;

    @Test
    @DisplayName("findUserCollectedNoteIds - 有收藏")
    void findUserCollectedNoteIds_hasCollected() {
        when(collectionNoteMapper.findUserCollectedNoteIds(1L, List.of(1, 2, 3)))
                .thenReturn(List.of(1, 3));

        Set<Integer> result = collectionNoteService.findUserCollectedNoteIds(1L, List.of(1, 2, 3));
        assertEquals(2, result.size());
        assertTrue(result.contains(1));
        assertTrue(result.contains(3));
        assertFalse(result.contains(2));
    }

    @Test
    @DisplayName("findUserCollectedNoteIds - 无收藏")
    void findUserCollectedNoteIds_noCollected() {
        when(collectionNoteMapper.findUserCollectedNoteIds(1L, List.of(1, 2)))
                .thenReturn(List.of());

        Set<Integer> result = collectionNoteService.findUserCollectedNoteIds(1L, List.of(1, 2));
        assertTrue(result.isEmpty());
    }


}

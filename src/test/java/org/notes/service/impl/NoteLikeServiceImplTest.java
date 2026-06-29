package org.notes.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.notes.exception.NotFoundException;
import org.notes.mapper.NoteLikeMapper;
import org.notes.mapper.NoteMapper;
import org.notes.scope.RequestScopeData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteLikeServiceImplTest {

    @Mock
    private NoteLikeMapper noteLikeMapper;
    @Mock
    private NoteMapper noteMapper;
    @Mock
    private RequestScopeData requestScopeData;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NoteLikeServiceImpl noteLikeService;

    @Test
    void findUserLikedNoteIds_returnsSet() {
        when(noteLikeMapper.findUserLikedNoteIds(1L, List.of(1, 2, 3))).thenReturn(List.of(1, 3));

        Set<Integer> result = noteLikeService.findUserLikedNoteIds(1L, List.of(1, 2, 3));

        assertEquals(Set.of(1, 3), result);
    }

    @Test
    void likeNote_throwsWhenNoteMissing() {
        when(noteMapper.findById(9)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> noteLikeService.likeNote(9));
    }

    @Test
    void unlikeNote_throwsWhenNoteMissing() {
        when(noteMapper.findById(9)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> noteLikeService.unlikeNote(9));
    }
}

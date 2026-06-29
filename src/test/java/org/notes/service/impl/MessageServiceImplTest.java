package org.notes.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.notes.mapper.CommentMapper;
import org.notes.mapper.MessageMapper;
import org.notes.mapper.NoteMapper;
import org.notes.mapper.QuestionMapper;
import org.notes.model.dto.message.MessageDTO;
import org.notes.model.entity.Message;
import org.notes.scope.RequestScopeData;
import org.notes.service.UserService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageMapper messageMapper;
    @Mock
    private UserService userService;
    @Mock
    private RequestScopeData requestScopeData;
    @Mock
    private NoteMapper noteMapper;
    @Mock
    private QuestionMapper questionMapper;
    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private MessageServiceImpl messageService;

    @Test
    void createMessage_defaultsNullContentToEmptyString() {
        MessageDTO dto = new MessageDTO();
        dto.setContent(null);
        when(messageMapper.insert(org.mockito.ArgumentMatchers.any(Message.class))).thenReturn(1);

        assertEquals(1, messageService.createMessage(dto));

        verify(messageMapper).insert(org.mockito.ArgumentMatchers.argThat(message -> "".equals(message.getContent())));
    }

    @Test
    void getMessages_returnsEmptyList() {
        when(requestScopeData.getUserId()).thenReturn(1L);
        when(messageMapper.selectByUserId(1L)).thenReturn(Collections.emptyList());

        assertTrue(messageService.getMessages().isEmpty());
    }

    @Test
    void markBatchAsRead_usesCurrentUser() {
        when(requestScopeData.getUserId()).thenReturn(1L);

        messageService.markBatchAsRead(List.of(1, 2));

        verify(messageMapper).markBatchAsRead(List.of(1, 2), 1L);
    }

    @Test
    void getUnreadCount_usesCurrentUser() {
        when(requestScopeData.getUserId()).thenReturn(1L);
        when(messageMapper.countUnread(1L)).thenReturn(3);

        assertEquals(3, messageService.getUnreadCount());
    }
}

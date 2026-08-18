package org.notes.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.notes.exception.NotFoundException;
import org.notes.mapper.DlxMessageMapper;
import org.notes.model.entity.DlxMessage;
import org.notes.service.ReliableRabbitPublisher;
import org.springframework.amqp.core.Message;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DlxMessageServiceImplTest {

    @Mock
    private DlxMessageMapper dlxMessageMapper;
    @Mock
    private ReliableRabbitPublisher reliableRabbitPublisher;

    @InjectMocks
    private DlxMessageServiceImpl dlxMessageService;

    private DlxMessage dlxMessage;

    @BeforeEach
    void setUp() {
        dlxMessage = new DlxMessage();
        dlxMessage.setId(1L);
        dlxMessage.setOriginQueue("email.queue");
        dlxMessage.setMessageBody("{\"email\":\"a@example.com\"}");
    }

    @Test
    void listMessages_returnsLatestMessages() {
        when(dlxMessageMapper.findAll()).thenReturn(List.of(dlxMessage));

        assertEquals(1, dlxMessageService.listMessages().size());
    }

    @Test
    void retryMessage_republishesRawMessageAndMarksDeadRecordRetried() {
        when(dlxMessageMapper.findById(1L)).thenReturn(dlxMessage);

        dlxMessageService.retryMessage(1L);

        verify(reliableRabbitPublisher).sendRaw(eq("email.queue"), any(Message.class), isNull());
        verify(dlxMessageMapper).markRetried(1L);
        verify(dlxMessageMapper, never()).deleteById(1L);
    }

    @Test
    void retryMessage_throwsWhenMessageMissing() {
        when(dlxMessageMapper.findById(9L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> dlxMessageService.retryMessage(9L));
    }

    @Test
    void retryMessage_keepsRecordAndMarksFailureWhenPublishIsNotConfirmed() {
        when(dlxMessageMapper.findById(1L)).thenReturn(dlxMessage);
        doThrow(new IllegalStateException("nack"))
                .when(reliableRabbitPublisher).sendRaw(eq("email.queue"), any(Message.class), isNull());

        assertThrows(IllegalStateException.class, () -> dlxMessageService.retryMessage(1L));

        verify(dlxMessageMapper).markRetryFailed(eq(1L), contains("nack"));
        verify(dlxMessageMapper, never()).markRetried(anyLong());
        verify(dlxMessageMapper, never()).deleteById(anyLong());
    }

    @Test
    void deleteMessage_removesRecord() {
        dlxMessageService.deleteMessage(1L);

        verify(dlxMessageMapper).deleteById(1L);
    }
}

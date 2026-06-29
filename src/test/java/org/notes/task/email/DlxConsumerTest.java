package org.notes.task.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.notes.mapper.DlxMessageMapper;
import org.notes.model.entity.DlxMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DlxConsumerTest {

    @Mock
    private DlxMessageMapper dlxMessageMapper;

    @InjectMocks
    private DlxConsumer dlxConsumer;

    @Test
    void processDlxMessage_recordsOriginalQueueFromXDeathHeader() {
        Message message = MessageBuilder.withBody("{\"email\":\"a@example.com\"}".getBytes(StandardCharsets.UTF_8))
                .setHeader("x-death", List.of(Map.of(
                        "queue", "email.queue",
                        "reason", "rejected",
                        "count", 3L
                )))
                .build();

        dlxConsumer.processDlxMessage(message);

        ArgumentCaptor<DlxMessage> captor = ArgumentCaptor.forClass(DlxMessage.class);
        verify(dlxMessageMapper).insert(captor.capture());
        assertEquals("email.queue", captor.getValue().getOriginQueue());
        assertTrue(captor.getValue().getErrorMessage().contains("rejected"));
        assertTrue(captor.getValue().getErrorMessage().contains("3"));
    }
}

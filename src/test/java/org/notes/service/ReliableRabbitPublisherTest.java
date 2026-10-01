package org.notes.service;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReliableRabbitPublisherTest {
    @Test
    void nackDoesNotReportSuccess() {
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        doAnswer(call -> {
            CorrelationData correlation = call.getArgument(3);
            correlation.getFuture().set(new CorrelationData.Confirm(false, "unavailable"));
            return null;
        }).when(rabbit).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
        assertThrows(IllegalStateException.class, () -> new ReliableRabbitPublisher(rabbit)
                .sendRaw("notification.queue", new Message(new byte[0], new MessageProperties()), "trace"));
    }
}

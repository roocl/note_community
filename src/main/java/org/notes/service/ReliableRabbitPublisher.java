package org.notes.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ReliableRabbitPublisher {

    public static final String TRACE_ID_HEADER = "traceId";
    private static final long CONFIRM_TIMEOUT_SECONDS = 5;

    private final RabbitTemplate rabbitTemplate;

    public void send(String queue, Object payload) {
        send(queue, payload, MDC.get(TRACE_ID_HEADER));
    }

    public void send(String queue, Object payload, String traceId) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        MessagePostProcessor tracePostProcessor = message -> addTraceId(message, traceId);
        rabbitTemplate.convertAndSend("", queue, payload, tracePostProcessor, correlationData);
        awaitConfirm(queue, correlationData);
    }

    public void sendRaw(String queue, Message message, String traceId) {
        addTraceId(message, traceId);
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.send("", queue, message, correlationData);
        awaitConfirm(queue, correlationData);
    }

    private Message addTraceId(Message message, String traceId) {
        if (traceId != null && !traceId.isBlank()) {
            message.getMessageProperties().setHeader(TRACE_ID_HEADER, traceId);
        }
        return message;
    }

    private void awaitConfirm(String queue, CorrelationData correlationData) {
        try {
            CorrelationData.Confirm confirm = correlationData.getFuture()
                    .get(CONFIRM_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!confirm.isAck()) {
                throw new IllegalStateException("RabbitMQ rejected message for queue " + queue
                        + ": " + confirm.getReason());
            }
            if (correlationData.getReturned() != null) {
                throw new IllegalStateException("RabbitMQ returned unroutable message for queue " + queue);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for RabbitMQ confirm", e);
        } catch (Exception e) {
            throw new IllegalStateException("RabbitMQ publish was not confirmed for queue " + queue, e);
        }
    }

}

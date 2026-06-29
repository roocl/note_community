package org.notes.task.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.notes.config.RabbitMQConfig;
import org.notes.mapper.DlxMessageMapper;
import org.notes.model.entity.DlxMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DlxConsumer {

    private final DlxMessageMapper dlxMessageMapper;

    @RabbitListener(queues = {
            RabbitMQConfig.DLX_EMAIL_QUEUE,
            RabbitMQConfig.DLX_NOTIFICATION_QUEUE,
            RabbitMQConfig.DLX_WELCOME_EMAIL_QUEUE
    })
    public void processDlxMessage(org.springframework.amqp.core.Message failedMessage) {
        DeadLetterInfo deadLetterInfo = resolveDeadLetterInfo(failedMessage);
        String body = new String(failedMessage.getBody(), StandardCharsets.UTF_8);

        log.error("收到死信消息，来源队列={}，消息体={}", deadLetterInfo.originQueue(), body);

        try {
            DlxMessage dlxMessage = new DlxMessage();
            dlxMessage.setOriginQueue(deadLetterInfo.originQueue());
            dlxMessage.setMessageBody(body);
            dlxMessage.setErrorMessage(deadLetterInfo.errorMessage());
            dlxMessageMapper.insert(dlxMessage);
            log.info("死信消息已持久化，id={}", dlxMessage.getId());
        } catch (Exception e) {
            log.error("死信消息持久化失败", e);
            throw new RuntimeException("死信处理失败", e);
        }
    }

    private DeadLetterInfo resolveDeadLetterInfo(org.springframework.amqp.core.Message failedMessage) {
        Object xDeathHeader = failedMessage.getMessageProperties().getHeaders().get("x-death");
        if (xDeathHeader instanceof List<?> deaths && !deaths.isEmpty() && deaths.get(0) instanceof Map<?, ?> death) {
            String originQueue = headerValue(death, "queue", failedMessage.getMessageProperties().getConsumerQueue());
            String reason = headerValue(death, "reason", "unknown");
            String count = headerValue(death, "count", "unknown");
            return new DeadLetterInfo(originQueue, "死信原因=" + reason + "，重试次数=" + count);
        }
        return new DeadLetterInfo(failedMessage.getMessageProperties().getConsumerQueue(), "重试耗尽，进入死信队列");
    }

    private String headerValue(Map<?, ?> headers, String key, String fallback) {
        Object value = headers.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private record DeadLetterInfo(String originQueue, String errorMessage) {
    }
}

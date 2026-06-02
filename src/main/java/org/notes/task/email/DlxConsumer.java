package org.notes.task.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.notes.config.RabbitMQConfig;
import org.notes.mapper.DlxMessageMapper;
import org.notes.model.entity.DlxMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

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
        String originQueue = failedMessage.getMessageProperties().getConsumerQueue();
        String body = new String(failedMessage.getBody());

        log.error("收到死信消息，来源队列={}，消息体={}", originQueue, body);

        try {
            DlxMessage dlxMessage = new DlxMessage();
            dlxMessage.setOriginQueue(originQueue);
            dlxMessage.setMessageBody(body);
            dlxMessage.setErrorMessage("重试耗尽，进入死信队列");
            dlxMessageMapper.insert(dlxMessage);
            log.info("死信消息已持久化，id={}", dlxMessage.getId());
        } catch (Exception e) {
            log.error("死信消息持久化失败", e);
            throw new RuntimeException("死信处理失败", e);
        }
    }
}

package org.notes.task.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.notes.config.RabbitMQConfig;
import org.notes.model.dto.message.MessageDTO;
import org.notes.service.MessageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationTaskConsumer {

    private final MessageService messageService;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void processNotification(NotificationTask task) {
        log.info("收到通知消息：type={}, sender={}, receiver={}", task.getType(), task.getSenderId(), task.getReceiverId());

        try {
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setReceiverId(task.getReceiverId());
            messageDTO.setSenderId(task.getSenderId());
            messageDTO.setType(task.getType());
            messageDTO.setTargetId(task.getTargetId());
            messageDTO.setTargetType(task.getTargetType());
            messageDTO.setContent(task.getContent() != null ? task.getContent() : "");
            messageDTO.setIsRead(false);

            messageService.createMessage(messageDTO);
            log.info("通知消息处理成功");
        } catch (Exception e) {
            log.error("通知消息处理失败，type={}, receiver={}", task.getType(), task.getReceiverId(), e);
            throw new RuntimeException("通知消息处理失败，触发重试", e);
        }
    }
}

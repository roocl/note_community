package org.notes.model.enums.outbox;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.notes.config.RabbitMQConfig;

@Getter
@RequiredArgsConstructor
public enum OutboxEventType {
    NOTIFICATION(RabbitMQConfig.NOTIFICATION_QUEUE),
    WELCOME_EMAIL(RabbitMQConfig.WELCOME_EMAIL_QUEUE);

    private final String queue;
}

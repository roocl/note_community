package org.notes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxDispatcher {
    private final OutboxService outbox;
    private final ReliableRabbitPublisher publisher;

    @Scheduled(fixedDelayString = "${outbox.poll-delay-ms:1000}")
    public void dispatch() {
        for (int count = 0; count < 20; count++) {
            var candidate = outbox.claim();
            if (candidate.isEmpty()) return;
            var delivery = candidate.get();
            try {
                MessageProperties properties = new MessageProperties();
                properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                properties.setContentEncoding(StandardCharsets.UTF_8.name());
                properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                properties.setMessageId(delivery.eventId());
                publisher.sendRaw(delivery.queue(), new Message(delivery.payload().getBytes(StandardCharsets.UTF_8), properties),
                        delivery.traceId());
                outbox.complete(delivery);
            } catch (Exception failure) {
                outbox.retry(delivery, failure);
                log.warn("Outbox delivery failed, eventId={}", delivery.eventId(), failure);
                if (Thread.currentThread().isInterrupted()) return;
            }
        }
    }
}

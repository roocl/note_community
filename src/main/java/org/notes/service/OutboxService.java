package org.notes.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.notes.mapper.OutboxMapper;
import org.notes.model.entity.OutboxEvent;
import org.notes.model.enums.outbox.OutboxEventType;
import org.notes.task.email.WelcomeEmailTask;
import org.notes.task.notification.NotificationTask;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.UUID;

@Service
public class OutboxService {
    private final OutboxMapper mapper;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate deliveryTransaction;

    public OutboxService(OutboxMapper mapper, ObjectMapper objectMapper, PlatformTransactionManager manager) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        deliveryTransaction = new TransactionTemplate(manager);
        deliveryTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        deliveryTransaction.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public void record(NotificationTask task) {
        task.setEventId(UUID.randomUUID().toString());
        persist(task.getEventId(), OutboxEventType.NOTIFICATION, task);
    }

    public void record(WelcomeEmailTask task) {
        task.setEventId(UUID.randomUUID().toString());
        persist(task.getEventId(), OutboxEventType.WELCOME_EMAIL, task);
    }

    private void persist(String eventId, OutboxEventType type, Object payload) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            throw new IllegalStateException("Outbox event requires a writable business transaction");
        }
        try {
            OutboxEvent event = new OutboxEvent();
            event.setEventId(eventId);
            event.setEventType(type);
            event.setPayload(objectMapper.writeValueAsString(payload));
            event.setTraceId(MDC.get(ReliableRabbitPublisher.TRACE_ID_HEADER));
            mapper.insert(event);
        } catch (JsonProcessingException failure) {
            throw new IllegalArgumentException("Cannot serialize outbox event", failure);
        }
    }

    public Optional<Delivery> claim() {
        return deliveryTransaction.execute(status -> {
            OutboxEvent event = mapper.findAvailableForUpdate();
            if (event == null) return Optional.empty();
            Delivery delivery = new Delivery(event.getEventId(), event.getEventType().getQueue(), event.getPayload(),
                    event.getTraceId(), UUID.randomUUID().toString(), event.getAttempts() + 1);
            mapper.claim(delivery.eventId(), delivery.leaseToken());
            return Optional.of(delivery);
        });
    }

    public void complete(Delivery delivery) {
        deliveryTransaction.executeWithoutResult(status -> mapper.complete(delivery.eventId(), delivery.leaseToken()));
    }

    public void retry(Delivery delivery, Exception failure) {
        long delaySeconds = Math.min(300, 1L << Math.min(delivery.attempts(), 9));
        deliveryTransaction.executeWithoutResult(status -> mapper.retry(delivery.eventId(), delivery.leaseToken(),
                delaySeconds, failure.toString()));
    }

    public record Delivery(String eventId, String queue, String payload, String traceId, String leaseToken, int attempts) {}

}

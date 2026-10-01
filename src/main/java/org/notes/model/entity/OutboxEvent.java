package org.notes.model.entity;

import lombok.Data;
import org.notes.model.enums.outbox.OutboxEventType;

@Data
public class OutboxEvent {
    private String eventId;
    private OutboxEventType eventType;
    private String payload;
    private String traceId;
    private int attempts;
}

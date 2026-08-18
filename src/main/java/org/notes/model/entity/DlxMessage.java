package org.notes.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DlxMessage {
    private Long id;
    private String originQueue;
    private String messageBody;
    private String errorMessage;
    private String traceId;
    private String status;
    private Integer retryCount;
    private LocalDateTime lastRetryAt;
    private LocalDateTime createdAt;
}

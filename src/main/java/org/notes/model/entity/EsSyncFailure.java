package org.notes.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EsSyncFailure {
    private Long id;
    private String entityType;
    private Long entityId;
    private String operation;
    private String status;
    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

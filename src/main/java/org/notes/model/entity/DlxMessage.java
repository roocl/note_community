package org.notes.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DlxMessage {
    private Long id;
    private String originQueue;
    private String messageBody;
    private String errorMessage;
    private LocalDateTime createdAt;
}

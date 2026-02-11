package org.notes.model.dto.message;

import lombok.Data;

import java.util.List;

@Data
public class ReadMessageBatchRequest {
    private List<Integer> messageIds;
}

package org.notes.model.dto.message;

import lombok.Data;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;

@Data
public class MessageQueryParams {
    /**
     * 消息类型
     */
    private String type;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 当前页码，从1开始
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 10;

    /**
     * 排序字段，默认创建时间
     */
    private String sortField = "created_at";

    /**
     * 排序方向，默认降序
     */
    private String sortOrder = "desc";
}
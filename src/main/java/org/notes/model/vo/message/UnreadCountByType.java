package org.notes.model.vo.message;

import lombok.Data;

@Data
public class UnreadCountByType {
    /**
     * 消息类型
     */
    private String type;

    /**
     * 未读数量
     */
    private Integer count;
}

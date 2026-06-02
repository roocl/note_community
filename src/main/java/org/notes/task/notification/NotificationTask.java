package org.notes.task.notification;

import lombok.Data;

import java.io.Serializable;

@Data
public class NotificationTask implements Serializable {

    private Long receiverId;

    private Long senderId;

    private Integer type;

    private Integer targetId;

    private Integer targetType;

    private String content;
}

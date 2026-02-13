package org.notes.event;

import lombok.Getter;
import org.notes.model.vo.message.MessageVO;
import org.springframework.context.ApplicationEvent;

@Getter
public class MessageEvent extends ApplicationEvent {
    //todo 完善event调用
    private final MessageVO message;
    private final Long receiverId;
    private final String eventType;

    public MessageEvent(Object source, MessageVO message, Long receiverId, String eventType) {
        super(source);
        this.message = message;
        this.receiverId = receiverId;
        this.eventType = eventType;
    }

    public static MessageEvent createCommentEvent(Object source, MessageVO message, Long receiverId) {
        return new MessageEvent(source, message, receiverId, "COMMENT");
    }

    public static MessageEvent createLikeEvent(Object source, MessageVO message, Long receiverId) {
        return new MessageEvent(source, message, receiverId, "LIKE");
    }

    public static MessageEvent createSystemEvent(Object source, MessageVO message, Long receiverId) {
        return new MessageEvent(source, message, receiverId, "SYSTEM");
    }
}

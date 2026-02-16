package org.notes.service;

import org.notes.model.dto.message.MessageDTO;
import org.notes.model.vo.message.MessageVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface MessageService {

    Integer createMessage(MessageDTO messageDTO);

    List<MessageVO> getMessages();

    void markAsRead(Integer messageId);

    void markBatchAsRead(List<Integer> messageIds);

    void markAllAsRead();

    void deleteMessage(Integer messageId);

    Integer getUnreadCount();
}

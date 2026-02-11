package org.notes.service;

import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.message.MessageDTO;
import org.notes.model.vo.message.MessageVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface MessageService {

    Integer createMessage(MessageDTO messageDTO);

    ApiResponse<List<MessageVO>> getMessages();

    ApiResponse<EmptyVO> markAsRead(Integer messageId);

    ApiResponse<EmptyVO> markBatchAsRead(List<Integer> messageIds);

    ApiResponse<EmptyVO> markAllAsRead();

    ApiResponse<EmptyVO> deleteMessage(Integer messageId);

    ApiResponse<Integer> getUnreadCount();
}

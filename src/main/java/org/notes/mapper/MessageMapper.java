package org.notes.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.notes.model.dto.message.MessageQueryParams;
import org.notes.model.entity.Message;
import org.notes.model.vo.message.UnreadCountByType;

import java.util.List;

@Mapper
public interface MessageMapper {

    int insert(Message message);

    List<Message> selectByUserId(Long userId);

    List<Message> selectByParams(@Param("userId") Long userId, @Param("params") MessageQueryParams params, @Param("offset") int offset);

    int countByParams(@Param("userId") Long userId, @Param("params") MessageQueryParams params);

    int markAsRead(@Param("messageId") Integer messageId, @Param("userId") Long userId);

    int markAllAsRead(@Param("userId") Long userId);

    int markBatchAsRead(@Param("messageIds") List<Integer> messageIds, @Param("userId") Long userId);

    int deleteMessage(@Param("messageId") Integer messageId, @Param("userId") Long userId);

    int countUnread(@Param("userId") Long userId);

    List<UnreadCountByType> countUnreadByType(@Param("userId") Long userId);
}

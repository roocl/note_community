package org.notes.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.notes.model.entity.OutboxEvent;

@Mapper
public interface OutboxMapper {
    int insert(OutboxEvent event);
    OutboxEvent findAvailableForUpdate();
    int claim(@Param("eventId") String eventId, @Param("leaseToken") String leaseToken);
    int complete(@Param("eventId") String eventId, @Param("leaseToken") String leaseToken);
    int retry(@Param("eventId") String eventId, @Param("leaseToken") String leaseToken,
              @Param("delaySeconds") long delaySeconds, @Param("error") String error);
}

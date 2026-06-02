package org.notes.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.notes.model.entity.DlxMessage;
import java.util.List;

@Mapper
public interface DlxMessageMapper {
    int insert(DlxMessage message);
    DlxMessage findById(@Param("id") Long id);
    List<DlxMessage> findAll();
    int deleteById(@Param("id") Long id);
}

package org.notes.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.notes.model.entity.EsSyncFailure;

import java.util.List;

@Mapper
public interface EsSyncFailureMapper {

    int insert(EsSyncFailure failure);

    EsSyncFailure findById(@Param("id") Long id);

    EsSyncFailure findOpen(@Param("entityType") String entityType,
                           @Param("entityId") Long entityId,
                           @Param("operation") String operation);

    List<EsSyncFailure> findAll();

    List<EsSyncFailure> findPending(@Param("limit") int limit);

    int markSuccess(@Param("id") Long id);

    int markFailed(@Param("id") Long id, @Param("errorMessage") String errorMessage, @Param("retryCount") int retryCount);

    int deleteById(@Param("id") Long id);
}

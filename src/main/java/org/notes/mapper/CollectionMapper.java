package org.notes.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.notes.model.entity.Collection;

import java.util.List;

@Mapper
public interface CollectionMapper {

    Collection findById(@Param("collectionId") Integer collectionId);

    List<Collection> findByCreatorId(@Param("creatorId") Long creatorId);

    Collection findByIdAndCreatorId(@Param("collectionId") Integer collectionId, @Param("creatorId") Long creatorId);

    int countByCreatorIdAndNoteId(
            @Param("creatorId") Long creatorId,
            @Param("noteId") Integer noteId);

    int insert(Collection collection);

    int update(Collection collection);

    int deleteById(@Param("collectionId") Integer collectionId);
}
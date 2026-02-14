package org.notes.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.notes.model.entity.CollectionNote;

import java.util.List;
import java.util.Set;

@Mapper
public interface CollectionNoteMapper {

    List<Integer> findUserCollectedNoteIds(
            @Param("userId") Long userId,
            @Param("noteIds") List<Integer> noteIds
    );

    Set<Integer> filterCollectionIdsByNoteId(
            @Param("noteId") Integer noteId,
            @Param("collectionIds") List<Integer> collectionIds);

    List<Integer> findNoteIdsByCollectionId(@Param("collectionId") Integer collectionId);

    CollectionNote findByCollectionIdAndNoteId(
            @Param("collectionId") Integer collectionId,
            @Param("noteId") Integer noteId);

    int insert(CollectionNote collectionNote);

    int delete(CollectionNote collectionNote);

    int deleteByCollectionId(@Param("collectionId") Integer collectionId);

    int deleteByCollectionIdAndNoteId(
            @Param("collectionId") Integer collectionId,
            @Param("noteId") Integer noteId);
}
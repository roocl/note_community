package org.notes.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.notes.model.entity.NoteLike;

import java.util.List;

@Mapper
public interface NoteLikeMapper {

    int insert(NoteLike noteLike);

    int delete(NoteLike noteLike);

    List<Integer> findUserLikedNoteIds(
            @Param("userId") Long userId,
            @Param("noteIds") List<Integer> noteIds
    );

    NoteLike findByUserIdAndNoteId(
            @Param("userId") Long userId,
            @Param("noteId") Integer noteId
    );
}

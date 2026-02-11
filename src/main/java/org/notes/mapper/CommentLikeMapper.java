package org.notes.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.notes.model.entity.CommentLike;

import java.util.List;
import java.util.Set;

@Mapper
public interface CommentLikeMapper {

    void insert(CommentLike commentLike);

    void delete(@Param("commentId") Integer commentId, @Param("userId") Long userId);

    Set<Integer> findUserLikedCommentIds(@Param("userId") Long userId,
                                         @Param("commentIds") List<Integer> commentIds);
}

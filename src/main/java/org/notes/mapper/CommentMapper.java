package org.notes.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.notes.model.entity.Comment;
import org.notes.model.dto.comment.CommentQueryParams;

import java.util.List;

@Mapper
public interface CommentMapper {

    void insert(Comment comment);

    void update(Comment comment);

    void deleteById(Integer commentId);

    Comment findById(Integer commentId);

    List<Comment> findByNoteId(Integer noteId);

    List<Comment> findByQueryParam(@Param("params") CommentQueryParams params,
                                   @Param("pageSize") Integer pageSize,
                                   @Param("offset") Integer offset);

    int countByQueryParam(@Param("params") CommentQueryParams params);

    void incrementLikeCount(Integer commentId);

    void decrementLikeCount(Integer commentId);

    void incrementReplyCount(Integer commentId);

    void decrementReplyCount(Integer commentId);
}

package org.notes.service;

import org.notes.model.base.PageResult;
import org.notes.model.dto.comment.CommentQueryParams;
import org.notes.model.dto.comment.CreateCommentRequest;
import org.notes.model.dto.comment.UpdateCommentRequest;
import org.notes.model.vo.comment.CommentVO;

import java.util.List;

public interface CommentService {

    Integer createComment(CreateCommentRequest request);

    void updateComment(Integer commentId, UpdateCommentRequest request);

    void deleteComment(Integer commentId);

    PageResult<List<CommentVO>> getComments(CommentQueryParams params);

    void likeComment(Integer commentId);

    void unlikeComment(Integer commentId);
}

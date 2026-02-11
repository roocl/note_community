package org.notes.service;

import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.comment.CommentQueryParams;
import org.notes.model.dto.comment.CreateCommentRequest;
import org.notes.model.dto.comment.UpdateCommentRequest;
import org.notes.model.vo.comment.CommentVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface CommentService {

    ApiResponse<Integer> createComment(CreateCommentRequest request);

    ApiResponse<EmptyVO> updateComment(Integer commentId, UpdateCommentRequest request);

    ApiResponse<EmptyVO> deleteComment(Integer commentId);

    ApiResponse<List<CommentVO>> getComments(CommentQueryParams params);

    ApiResponse<EmptyVO> likeComment(Integer commentId);

    ApiResponse<EmptyVO> unlikeComment(Integer commentId);
}
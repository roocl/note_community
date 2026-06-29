package org.notes.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.notes.exception.ForbiddenException;
import org.notes.exception.NotFoundException;
import org.notes.mapper.CommentLikeMapper;
import org.notes.mapper.CommentMapper;
import org.notes.mapper.MessageMapper;
import org.notes.mapper.NoteMapper;
import org.notes.mapper.UserMapper;
import org.notes.model.base.PageResult;
import org.notes.model.dto.comment.CommentQueryParams;
import org.notes.model.dto.comment.UpdateCommentRequest;
import org.notes.model.entity.Comment;
import org.notes.model.vo.comment.CommentVO;
import org.notes.scope.RequestScopeData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentMapper commentMapper;
    @Mock
    private NoteMapper noteMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private CommentLikeMapper commentLikeMapper;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private RequestScopeData requestScopeData;
    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void updateComment_updatesOwnComment() {
        Comment comment = new Comment();
        comment.setCommentId(1);
        comment.setAuthorId(1L);
        UpdateCommentRequest request = new UpdateCommentRequest();
        request.setContent("updated");
        when(requestScopeData.getUserId()).thenReturn(1L);
        when(commentMapper.findById(1)).thenReturn(comment);

        commentService.updateComment(1, request);

        verify(commentMapper).update(any(Comment.class));
    }

    @Test
    void updateComment_throwsWhenCommentMissing() {
        when(requestScopeData.getUserId()).thenReturn(1L);
        when(commentMapper.findById(9)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> commentService.updateComment(9, new UpdateCommentRequest()));
    }

    @Test
    void updateComment_throwsWhenUserDoesNotOwnComment() {
        Comment comment = new Comment();
        comment.setAuthorId(2L);
        when(requestScopeData.getUserId()).thenReturn(1L);
        when(commentMapper.findById(1)).thenReturn(comment);

        assertThrows(ForbiddenException.class, () -> commentService.updateComment(1, new UpdateCommentRequest()));
    }

    @Test
    void getComments_returnsEmptyPageWhenNoComments() {
        CommentQueryParams params = new CommentQueryParams();
        params.setNoteId(1);
        params.setPage(1);
        params.setPageSize(10);
        when(commentMapper.findByNoteId(1)).thenReturn(Collections.emptyList());

        PageResult<List<CommentVO>> result = commentService.getComments(params);

        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getPagination().getTotal());
    }
}

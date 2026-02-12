package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.notes.annotation.NeedLogin;
import org.notes.mapper.*;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.base.Pagination;
import org.notes.model.dto.comment.CommentQueryParams;
import org.notes.model.dto.comment.CreateCommentRequest;
import org.notes.model.dto.comment.UpdateCommentRequest;
import org.notes.model.dto.message.MessageDTO;
import org.notes.model.entity.Comment;
import org.notes.model.entity.CommentLike;
import org.notes.model.entity.Note;
import org.notes.model.entity.User;
import org.notes.model.enums.message.MessageTargetType;
import org.notes.model.enums.message.MessageType;
import org.notes.model.vo.comment.CommentVO;
import org.notes.model.vo.user.UserActionVO;
import org.notes.scope.RequestScopeData;
import org.notes.service.CommentService;
import org.notes.service.MessageService;
import org.notes.utils.ApiResponseUtil;
import org.notes.utils.PaginationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;

    private final NoteMapper noteMapper;

    private final UserMapper userMapper;

    private final CommentLikeMapper commentLikeMapper;

    private final MessageService messageService;

    private final RequestScopeData requestScopeData;
    private final MessageMapper messageMapper;


    @Override
    @NeedLogin
    @Transactional
    public ApiResponse<Integer> createComment(CreateCommentRequest request) {
        try {
            Note note = noteMapper.findById(request.getNoteId());

            if (note == null) {
                return ApiResponseUtil.error("笔记不存在");
            }

            Long userId = requestScopeData.getUserId();
            Integer parentId = request.getParentId();

            Comment comment = new Comment();
            comment.setNoteId(request.getNoteId());
            comment.setAuthorId(userId);
            comment.setNoteId(request.getNoteId());
            comment.setParentId(parentId);
            comment.setContent(request.getContent());
            comment.setLikeCount(0);
            comment.setReplyCount(0);

            commentMapper.insert(comment);

            noteMapper.incrementCommentCount(request.getNoteId());

            if (parentId != null) {
                commentMapper.incrementReplyCount(request.getParentId());
            }

            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setReceiverId(note.getAuthorId());
            messageDTO.setSenderId(userId);
            messageDTO.setType(MessageType.COMMENT);
            messageDTO.setTargetId(request.getNoteId());
            if (parentId != null) {
                messageDTO.setTargetType(MessageTargetType.COMMENT);
            } else {
                messageDTO.setTargetType(MessageTargetType.NOTE);
            }
            messageDTO.setContent(request.getContent());
            messageDTO.setIsRead(false);

            messageService.createMessage(messageDTO);

            return ApiResponse.success(comment.getCommentId());
        } catch (Exception e) {
            log.error("创建评论失败", e);
            return ApiResponseUtil.error("创建评论失败: " + e.getMessage());
        }
    }

    @Override
    @NeedLogin
    @Transactional
    public ApiResponse<EmptyVO> updateComment(Integer commentId, UpdateCommentRequest request) {
        Long userId = requestScopeData.getUserId();
        Comment comment = commentMapper.findById(commentId);

        if (comment == null) {
            return ApiResponseUtil.error("评论不存在");
        }

        if (!Objects.equals(comment.getAuthorId(), userId)) {
            return ApiResponseUtil.error("没有权限修改该评论");
        }

        try {
            comment.setContent(request.getContent());
            commentMapper.update(comment);
            return ApiResponseUtil.success("更新评论成功");
        } catch (Exception e) {
            return ApiResponseUtil.error("更新评论失败");
        }
    }

    @Override
    @NeedLogin
    @Transactional
    public ApiResponse<EmptyVO> deleteComment(Integer commentId) {
        Long userId = requestScopeData.getUserId();
        Comment comment = commentMapper.findById(commentId);

        if (comment == null) {
            return ApiResponseUtil.error("评论不存在");
        }

        if (!Objects.equals(comment.getAuthorId(), userId)) {
            return ApiResponseUtil.error("没有权限删除该评论");
        }

        try {
            commentMapper.deleteById(commentId);
            return ApiResponseUtil.success("删除评论成功");
        } catch (Exception e) {
            return ApiResponseUtil.error("删除评论失败");
        }
    }

    @Override
    public ApiResponse<List<CommentVO>> getComments(CommentQueryParams params) {
        try {
            List<Comment> comments = commentMapper.findByNoteId(params.getNoteId());

            if (CollectionUtils.isEmpty(comments)) {
                return ApiResponseUtil.success("该笔记没有评论", Collections.emptyList());
            }

            List<Comment> firstLevel = comments.stream()
                    .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                    .sorted(Comparator.comparing(Comment::getCreatedAt))
                    .toList();

            int from = PaginationUtils.calculateOffset(params.getPage(), params.getPageSize());
            if (from >= firstLevel.size()) {
                return ApiResponseUtil.success("起始索引超出最大索引", Collections.emptyList());
            }

            int to = Math.min(from + params.getPageSize(), firstLevel.size());
            List<Comment> pagedFirst = firstLevel.subList(from, to);

            Map<Integer, List<Comment>> repliesMap = comments.stream()
                    .filter(c -> c.getParentId() != null)
                    .collect(Collectors.groupingBy(Comment::getParentId));

            List<Long> authorIds = comments.stream()
                    .map(Comment::getAuthorId)
                    .collect(Collectors.toList());

            Map<Long, User> authorMap = userMapper.findByIdBatch(authorIds)
                    .stream()
                    .collect(Collectors.toMap(User::getUserId, u -> u));

            Long userId = requestScopeData.getUserId();

            Set<Integer> likedSet;
            if (userId != null) {
                List<Integer> allCommentIds = comments.stream()
                        .map(Comment::getCommentId)
                        .toList();
                likedSet = new HashSet<>(commentLikeMapper.findUserLikedCommentIds(userId, allCommentIds));
            } else {
                likedSet = Collections.emptySet();
            }

            List<CommentVO> result = pagedFirst.stream()
                    .map(c -> toVO(c, repliesMap, authorMap, likedSet))
                    .toList();

            Pagination pagination = new Pagination(params.getPage(), params.getPageSize(), firstLevel.size());

            return ApiResponseUtil.success("获取评论列表成功", result, pagination);
        } catch (Exception e) {
            log.error("获取评论列表失败", e);
            return ApiResponseUtil.error("获取评论列表失败");
        }
    }

    private CommentVO toVO(Comment c,
                           Map<Integer, List<Comment>> repliesMap,
                           Map<Long, User> authorMap,
                           Set<Integer> likedSet) {
        CommentVO vo = new CommentVO();
        vo.setCommentId(c.getCommentId());
        vo.setNoteId(c.getNoteId());
        vo.setContent(c.getContent());
        vo.setLikeCount(c.getLikeCount());
        vo.setReplyCount(c.getReplyCount());
        vo.setCreatedAt(c.getCreatedAt());
        vo.setUpdatedAt(c.getUpdatedAt());

        User author = authorMap.get(c.getAuthorId());
        if (author != null) {
            CommentVO.SimpleAuthorVO a = new CommentVO.SimpleAuthorVO();
            a.setUserId(author.getUserId());
            a.setUsername(author.getUsername());
            a.setAvatarUrl(author.getAvatarUrl());
            vo.setAuthor(a);
        }

        if (!likedSet.isEmpty()) {
            UserActionVO userActionVO = new UserActionVO();
            userActionVO.setIsLiked(likedSet.contains(c.getCommentId()));
            vo.setUserActions(userActionVO);
        } else {
            vo.setUserActions(new UserActionVO());
            vo.getUserActions().setIsLiked(false);
        }

        List<Comment> children = repliesMap.get(c.getCommentId());
        if (children != null && !children.isEmpty()) {
            List<CommentVO> childVOs = children.stream()
                    .map(child -> toVO(child, repliesMap, authorMap, likedSet))
                    .toList();
            vo.setReplies(childVOs);
        } else {
            vo.setReplies(Collections.emptyList());
        }
        return vo;
    }

    @Override
    @NeedLogin
    @Transactional
    public ApiResponse<EmptyVO> likeComment(Integer commentId) {
        Long userId = requestScopeData.getUserId();
        Comment comment = commentMapper.findById(commentId);

        if (comment == null) {
            return ApiResponseUtil.error("评论不存在");
        }

        try {
            commentMapper.incrementLikeCount(commentId);

            CommentLike commentLike = new CommentLike();
            commentLike.setCommentId(commentId);
            commentLike.setUserId(userId);
            commentLikeMapper.insert(commentLike);

            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setReceiverId(comment.getAuthorId());
            messageDTO.setSenderId(userId);
            messageDTO.setType(MessageType.LIKE);
            //todo message数据库修改
            messageDTO.setTargetId(commentId);
            //todo 区分回复笔记或评论
            messageDTO.setTargetType(MessageTargetType.NOTE);
            messageDTO.setIsRead(false);

            messageService.createMessage(messageDTO);
            return ApiResponseUtil.success("点赞评论成功");
        } catch (Exception e) {
            return ApiResponseUtil.error("点赞评论失败");
        }
    }

    @Override
    @NeedLogin
    @Transactional
    public ApiResponse<EmptyVO> unlikeComment(Integer commentId) {
        Long userId = requestScopeData.getUserId();
        Comment comment = commentMapper.findById(commentId);

        if (comment == null) {
            return ApiResponseUtil.error("评论不存在");
        }

        try {
            commentMapper.decrementLikeCount(commentId);

            commentLikeMapper.delete(commentId, userId);

            return ApiResponseUtil.success("取消点赞评论成功");
        } catch (Exception e) {
            return ApiResponseUtil.error("取消点赞评论失败");
        }
    }
}

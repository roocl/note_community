package org.notes.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.base.PageResult;
import org.notes.model.dto.comment.CommentQueryParams;
import org.notes.model.dto.comment.CreateCommentRequest;
import org.notes.model.dto.comment.UpdateCommentRequest;
import org.notes.model.vo.comment.CommentVO;
import org.notes.service.CommentService;
import org.notes.utils.ApiResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "评论管理")
@Slf4j
@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @ApiOperation("获取评论列表")
    @GetMapping("/comments")
    public ApiResponse<List<CommentVO>> getComments(
            @Valid CommentQueryParams params) {
        PageResult<List<CommentVO>> result = commentService.getComments(params);
        return ApiResponseUtil.success("获取评论列表成功", result.getData(), result.getPagination());
    }

    @ApiOperation("创建评论")
    @PostMapping("/comments")
    public ApiResponse<Integer> createComment(
            @Valid @RequestBody CreateCommentRequest request) {
        return ApiResponseUtil.success("创建评论成功", commentService.createComment(request));
    }

    @ApiOperation("更新评论")
    @PatchMapping("/comments/{commentId}")
    public ApiResponse<EmptyVO> updateComment(
            @ApiParam("评论ID") @PathVariable("commentId") Integer commentId,
            @Valid @RequestBody UpdateCommentRequest request) {
        commentService.updateComment(commentId, request);
        return ApiResponseUtil.success("更新评论成功");
    }

    @ApiOperation("删除评论")
    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<EmptyVO> deleteComment(
            @ApiParam("评论ID") @PathVariable("commentId") Integer commentId) {
        commentService.deleteComment(commentId);
        return ApiResponseUtil.success("删除评论成功");
    }

    @ApiOperation("点赞评论")
    @PostMapping("/comments/{commentId}/like")
    public ApiResponse<EmptyVO> likeComment(
            @ApiParam("评论ID") @PathVariable("commentId") Integer commentId) {
        commentService.likeComment(commentId);
        return ApiResponseUtil.success("点赞评论成功");
    }

    @ApiOperation("取消点赞评论")
    @DeleteMapping("/comments/{commentId}/like")
    public ApiResponse<EmptyVO> unlikeComment(
            @ApiParam("评论ID") @PathVariable("commentId") Integer commentId) {
        commentService.unlikeComment(commentId);
        return ApiResponseUtil.success("取消点赞评论成功");
    }
}

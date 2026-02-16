package org.notes.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.service.NoteLikeService;
import org.notes.utils.ApiResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "笔记点赞")
@RestController
@RequestMapping("/api")
public class NoteLikeController {
    @Autowired
    private NoteLikeService noteLikeService;

    @ApiOperation("点赞笔记")
    @PostMapping("/like/note/{noteId}")
    public ApiResponse<EmptyVO> likeNote(
            @ApiParam("笔记ID") @PathVariable Integer noteId) {
        noteLikeService.likeNote(noteId);
        return ApiResponseUtil.success("点赞成功");
    }

    @ApiOperation("取消点赞笔记")
    @DeleteMapping("/like/note/{noteId}")
    public ApiResponse<EmptyVO> unlikeNote(
            @ApiParam("笔记ID") @PathVariable Integer noteId) {
        noteLikeService.unlikeNote(noteId);
        return ApiResponseUtil.success("取消点赞成功");
    }
}

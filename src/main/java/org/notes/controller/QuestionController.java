package org.notes.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.base.PageResult;
import org.notes.model.dto.question.*;
import org.notes.model.vo.question.CreateQuestionVO;
import org.notes.model.vo.question.QuestionNoteVO;
import org.notes.model.vo.question.QuestionUserVO;
import org.notes.model.vo.question.QuestionVO;
import org.notes.service.QuestionService;
import org.notes.utils.ApiResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * 题目表(Question)表控制层
 *
 * @author makejava
 * @since 2026-02-06 09:19:31
 */
@Api(tags = "题目管理")
@RestController
@RequestMapping("/api")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @ApiOperation("获取题目详情（用户端）")
    @GetMapping("/questions/{questionId}")
    public ApiResponse<QuestionNoteVO> userGetQuestion(
            @ApiParam("题目ID") @Min(value = 1, message = "questionId必须为正整数") @PathVariable Integer questionId) {
        return ApiResponseUtil.success("获取问题成功", questionService.userGetQuestion(questionId));
    }

    @ApiOperation("获取题目列表（用户端）")
    @GetMapping("/questions")
    public ApiResponse<List<QuestionUserVO>> userGetQuestions(@Valid QuestionQueryParams queryParam) {
        PageResult<List<QuestionUserVO>> result = questionService.userGetQuestions(queryParam);
        return ApiResponseUtil.success("获取用户问题列表成功", result.getData(), result.getPagination());
    }

    @ApiOperation("搜索题目")
    @PostMapping("/questions/search")
    public ApiResponse<List<QuestionVO>> searchQuestion(@Valid @RequestBody SearchQuestionBody body) {
        return ApiResponseUtil.success("搜索问题成功", questionService.searchQuestion(body));
    }

    @ApiOperation("获取题目列表（管理端）")
    @GetMapping("/admin/questions")
    public ApiResponse<List<QuestionVO>> adminGetQuestions(@Valid QuestionQueryParams queryParam) {
        PageResult<List<QuestionVO>> result = questionService.adminGetQuestions(queryParam);
        return ApiResponseUtil.success("获取问题列表成功", result.getData(), result.getPagination());
    }

    @ApiOperation("创建题目（管理端）")
    @PostMapping("/admin/questions")
    public ApiResponse<CreateQuestionVO> adminCreateQuestion(
            @Valid @RequestBody CreateQuestionBody createQuestionBody) {
        return ApiResponseUtil.success("创建问题成功", questionService.adminCreateQuestion(createQuestionBody));
    }

    @ApiOperation("更新题目（管理端）")
    @PatchMapping("/admin/questions/{questionId}")
    public ApiResponse<EmptyVO> adminUpdateQuestion(
            @ApiParam("题目ID") @Min(value = 1, message = "questionId必须为正整数") @PathVariable Integer questionId,
            @Valid @RequestBody UpdateQuestionBody updateQuestionBody) {
        questionService.adminUpdateQuestion(questionId, updateQuestionBody);
        return ApiResponseUtil.success("更新问题成功");
    }

    @ApiOperation("删除题目（管理端）")
    @DeleteMapping("/admin/questions/{questionId}")
    public ApiResponse<EmptyVO> adminDeleteQuestion(
            @ApiParam("题目ID") @Min(value = 1, message = "questionId 必须为正整数") @PathVariable Integer questionId) {
        questionService.adminDeleteQuestion(questionId);
        return ApiResponseUtil.success("删除问题成功");
    }
}

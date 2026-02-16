package org.notes.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.base.PageResult;
import org.notes.model.dto.questionList.CreateQuestionListItemBody;
import org.notes.model.dto.questionList.SortQuestionListItemBody;
import org.notes.model.dto.questionListItem.QuestionListItemQueryParams;
import org.notes.model.vo.questionListItem.CreateQuestionListItemVO;
import org.notes.model.vo.questionListItem.QuestionListItemUserVO;
import org.notes.model.vo.questionListItem.QuestionListItemVO;
import org.notes.service.QuestionListItemService;
import org.notes.utils.ApiResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Api(tags = "题单项管理")
@RestController
@RequestMapping("/api")
public class QuestionListItemController {

    @Autowired
    private QuestionListItemService questionListItemService;

    @ApiOperation("获取题单项列表（用户端）")
    @GetMapping("/questionlist-items")
    public ApiResponse<List<QuestionListItemUserVO>> userGetQuestionListItems(
            @Valid QuestionListItemQueryParams queryParams) {
        PageResult<List<QuestionListItemUserVO>> result = questionListItemService.userGetQuestionListItems(queryParams);
        return ApiResponseUtil.success("获取用户题单项列表成功", result.getData(), result.getPagination());
    }

    @ApiOperation("获取题单项列表（管理端）")
    @GetMapping("/admin/questionlist-items/{questionListId}")
    public ApiResponse<List<QuestionListItemVO>> getQuestionListItems(
            @ApiParam("题单ID") @Min(value = 1, message = "questionListId 必须为正整数") @PathVariable Integer questionListId) {
        return ApiResponseUtil.success("获取题单项列表成功", questionListItemService.adminGetQuestionListItems(questionListId));
    }

    @ApiOperation("创建题单项（管理端）")
    @PostMapping("/admin/questionlist-items")
    public ApiResponse<CreateQuestionListItemVO> createQuestionListItem(
            @Valid @RequestBody CreateQuestionListItemBody body) {
        return ApiResponseUtil.success("创建题单项成功", questionListItemService.createQuestionListItem(body));
    }

    @ApiOperation("删除题单项（管理端）")
    @DeleteMapping("/admin/questionlist-items/{questionListId}/{questionId}")
    public ApiResponse<EmptyVO> deleteQuestionListItem(
            @ApiParam("题单ID") @Min(value = 1, message = "questionListId 必须为正整数") @PathVariable Integer questionListId,
            @ApiParam("题目ID") @Min(value = 1, message = "questionId 必须为正整数") @PathVariable Integer questionId) {
        questionListItemService.deleteQuestionListItem(questionListId, questionId);
        return ApiResponseUtil.success("删除题单项成功");
    }

    @ApiOperation("题单项排序（管理端）")
    @PatchMapping("/admin/questionlist-items/sort")
    public ApiResponse<EmptyVO> sortQuestionListItem(
            @Valid @RequestBody SortQuestionListItemBody body) {
        questionListItemService.sortQuestionListItem(body);
        return ApiResponseUtil.success("题单项排序成功");
    }
}

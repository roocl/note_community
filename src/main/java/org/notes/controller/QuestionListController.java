package org.notes.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.questionList.CreateQuestionListBody;
import org.notes.model.dto.questionList.UpdateQuestionListBody;
import org.notes.model.entity.QuestionList;
import org.notes.model.vo.questionList.CreateQuestionListVO;
import org.notes.service.QuestionListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Api(tags = "题单管理")
@RestController
@RequestMapping("/api")
public class QuestionListController {

    @Autowired
    private QuestionListService questionListService;

    @ApiOperation("获取题单详情（管理端）")
    @GetMapping("/admin/questionlists/{questionListId}")
    public ApiResponse<QuestionList> getQuestionList(
            @ApiParam("题单ID") @Min(value = 1, message = "questionListId 必须为正整数") @PathVariable Integer questionListId) {
        return questionListService.getQuestionList(questionListId);
    }

    @ApiOperation("获取题单列表（管理端）")
    @GetMapping("/admin/questionlists")
    public ApiResponse<List<QuestionList>> getQuestionLists() {
        return questionListService.getQuestionLists();
    }

    @ApiOperation("创建题单（管理端）")
    @PostMapping("/admin/questionlists")
    public ApiResponse<CreateQuestionListVO> createQuestionList(@Valid @RequestBody CreateQuestionListBody body) {
        return questionListService.createQuestionList(body);
    }

    @ApiOperation("删除题单（管理端）")
    @DeleteMapping("/admin/questionlists/{questionListId}")
    public ApiResponse<EmptyVO> deleteQuestionList(
            @ApiParam("题单ID") @Min(value = 1, message = "questionListId 必须为正整数") @PathVariable Integer questionListId) {
        return questionListService.deleteQuestionList(questionListId);
    }

    @ApiOperation("更新题单（管理端）")
    @PatchMapping("/admin/questionlists/{questionListId}")
    public ApiResponse<EmptyVO> updateQuestionList(
            @ApiParam("题单ID") @Min(value = 1, message = "questionListId 必须为正整数") @PathVariable Integer questionListId,
            @Valid @RequestBody UpdateQuestionListBody body) {
        return questionListService.updateQuestionList(questionListId, body);
    }
}

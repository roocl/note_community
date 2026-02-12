package org.notes.controller;

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

@RestController
@RequestMapping("/api")
public class QuestionListController {

    @Autowired
    private QuestionListService questionListService;

    @GetMapping("/admin/questionlists/{questionListId}")
    public ApiResponse<QuestionList> getQuestionList(@Min(value = 1, message = "questionListId 必须为正整数")
                                                     @PathVariable Integer questionListId) {
        return questionListService.getQuestionList(questionListId);
    }

    @GetMapping("/admin/questionlists")
    public ApiResponse<List<QuestionList>> getQuestionLists() {
        return questionListService.getQuestionLists();
    }

    @PostMapping("/admin/questionlists")
    public ApiResponse<CreateQuestionListVO> createQuestionList(@Valid @RequestBody CreateQuestionListBody body) {
        return questionListService.createQuestionList(body);
    }

    @DeleteMapping("/admin/questionlists/{questionListId}")
    public ApiResponse<EmptyVO> deleteQuestionList(@Min(value = 1, message = "questionListId 必须为正整数")
                                                   @PathVariable Integer questionListId) {
        return questionListService.deleteQuestionList(questionListId);
    }

    @PatchMapping("/admin/questionlists/{questionListId}")
    public ApiResponse<EmptyVO> updateQuestionList(@Min(value = 1, message = "questionListId 必须为正整数")
                                                   @PathVariable Integer questionListId,
                                                   @Valid @RequestBody UpdateQuestionListBody body) {
        return questionListService.updateQuestionList(questionListId, body);
    }
}

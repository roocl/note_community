package org.notes.controller;

import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.question.*;
import org.notes.model.vo.question.CreateQuestionVO;
import org.notes.model.vo.question.QuestionNoteVO;
import org.notes.model.vo.question.QuestionUserVO;
import org.notes.model.vo.question.QuestionVO;
import org.notes.service.QuestionService;
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
@RestController
@RequestMapping("/api")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/questions/{questionId}")
    public ApiResponse<QuestionNoteVO> userGetQuestion(@Min(value = 1, message = "questionId必须为正整数")
                                                       @PathVariable Integer questionId) {
        return questionService.userGetQuestion(questionId);
    }

    @GetMapping("/questions")
    public ApiResponse<List<QuestionUserVO>> userGetQuestions(@Valid QuestionQueryParams queryParam) {
        return questionService.userGetQuestions(queryParam);
    }

    @PostMapping("/questions/search")
    public ApiResponse<List<QuestionVO>> searchQuestion(@Valid @RequestBody SearchQuestionBody body) {
        return questionService.searchQuestion(body);
    }

    @GetMapping("/admin/questions")
    public ApiResponse<List<QuestionVO>> adminGetQuestions(@Valid QuestionQueryParams queryParam) {
        return questionService.adminGetQuestions(queryParam);
    }

    @PostMapping("/admin/questions")
    public ApiResponse<CreateQuestionVO> adminCreateQuestion(@Valid @RequestBody CreateQuestionBody createQuestionBody) {
        return questionService.adminCreateQuestion(createQuestionBody);
    }

    @PatchMapping("/admin/questions/{questionId}")
    public ApiResponse<EmptyVO> adminUpdateQuestion(
            @Min(value = 1, message = "questionId必须为正整数") @PathVariable Integer questionId,
            @Valid @RequestBody UpdateQuestionBody updateQuestionBody) {
        return questionService.adminUpdateQuestion(questionId, updateQuestionBody);
    }

    @DeleteMapping("/admin/questions/{questionId}")
    public ApiResponse<QuestionUserVO> adminDeleteQuestion(@Min(value = 1, message = "questionId 必须为正整数")
                                                           @PathVariable Integer questionId) {
        return questionService.adminDeleteQuestion(questionId);
    }
}


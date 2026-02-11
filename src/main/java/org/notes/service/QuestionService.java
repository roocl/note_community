package org.notes.service;

import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.question.*;
import org.notes.model.entity.Question;
import org.notes.model.vo.question.CreateQuestionVO;
import org.notes.model.vo.question.QuestionNoteVO;
import org.notes.model.vo.question.QuestionUserVO;
import org.notes.model.vo.question.QuestionVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 题目表(Question)表服务接口
 *
 * @author makejava
 * @since 2026-02-06 09:19:46
 */
@Transactional
public interface QuestionService {

    Question findById(Integer questionId);

    Map<Integer, Question> getQuestionMapByIds(List<Integer> questionIds);

    ApiResponse<QuestionNoteVO> userGetQuestion(Integer questionId);

    ApiResponse<List<QuestionUserVO>> userGetQuestions(QuestionQueryParams queryParam);

    ApiResponse<List<QuestionVO>> searchQuestion(SearchQuestionBody body);

    ApiResponse<List<QuestionVO>> adminGetQuestions(QuestionQueryParams queryParam);

    ApiResponse<CreateQuestionVO> adminCreateQuestion(CreateQuestionBody createQuestionBody);


    ApiResponse<EmptyVO> adminUpdateQuestion(Integer questionId, UpdateQuestionBody updateQuestionBody);

    ApiResponse<QuestionUserVO> adminDeleteQuestion(Integer questionId);
}

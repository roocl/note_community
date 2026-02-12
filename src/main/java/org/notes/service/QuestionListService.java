package org.notes.service;

import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.questionList.CreateQuestionListBody;
import org.notes.model.dto.questionList.UpdateQuestionListBody;
import org.notes.model.entity.QuestionList;
import org.notes.model.vo.questionList.CreateQuestionListVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface QuestionListService {

    ApiResponse<QuestionList> getQuestionList(Integer questionListId);

    ApiResponse<List<QuestionList>> getQuestionLists();

    ApiResponse<CreateQuestionListVO> createQuestionList(CreateQuestionListBody body);

    ApiResponse<EmptyVO> deleteQuestionList(Integer questionListId);

    ApiResponse<EmptyVO> updateQuestionList(Integer questionListId, UpdateQuestionListBody body);
}


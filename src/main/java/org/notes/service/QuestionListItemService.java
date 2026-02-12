package org.notes.service;

import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.questionList.CreateQuestionListItemBody;
import org.notes.model.dto.questionList.SortQuestionListItemBody;
import org.notes.model.dto.questionListItem.QuestionListItemQueryParams;
import org.notes.model.vo.questionListItem.CreateQuestionListItemVO;
import org.notes.model.vo.questionListItem.QuestionListItemUserVO;
import org.notes.model.vo.questionListItem.QuestionListItemVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface QuestionListItemService {

    ApiResponse<List<QuestionListItemUserVO>> userGetQuestionListItems(QuestionListItemQueryParams queryParams);

    ApiResponse<List<QuestionListItemVO>> AdminGetQuestionListItems(Integer questionListId);

    ApiResponse<CreateQuestionListItemVO> createQuestionListItem(CreateQuestionListItemBody body);

    ApiResponse<EmptyVO> deleteQuestionListItem(Integer questionListId, Integer questionId);

    ApiResponse<EmptyVO> sortQuestionListItem(SortQuestionListItemBody body);
}
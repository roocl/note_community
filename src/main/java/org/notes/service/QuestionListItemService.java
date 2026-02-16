package org.notes.service;

import org.notes.model.base.PageResult;
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

    PageResult<List<QuestionListItemUserVO>> userGetQuestionListItems(QuestionListItemQueryParams queryParams);

    List<QuestionListItemVO> adminGetQuestionListItems(Integer questionListId);

    CreateQuestionListItemVO createQuestionListItem(CreateQuestionListItemBody body);

    void deleteQuestionListItem(Integer questionListId, Integer questionId);

    void sortQuestionListItem(SortQuestionListItemBody body);
}

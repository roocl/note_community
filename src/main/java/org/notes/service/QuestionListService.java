package org.notes.service;

import org.notes.model.dto.questionList.CreateQuestionListBody;
import org.notes.model.dto.questionList.UpdateQuestionListBody;
import org.notes.model.entity.QuestionList;
import org.notes.model.vo.questionList.CreateQuestionListVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface QuestionListService {

    QuestionList getQuestionList(Integer questionListId);

    List<QuestionList> getQuestionLists();

    CreateQuestionListVO createQuestionList(CreateQuestionListBody body);

    void deleteQuestionList(Integer questionListId);

    void updateQuestionList(Integer questionListId, UpdateQuestionListBody body);
}


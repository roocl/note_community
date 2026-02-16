package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import org.notes.exception.BaseException;
import org.notes.exception.NotFoundException;
import org.notes.mapper.QuestionListItemMapper;
import org.notes.mapper.QuestionListMapper;
import org.notes.model.dto.questionList.CreateQuestionListBody;
import org.notes.model.dto.questionList.UpdateQuestionListBody;
import org.notes.model.entity.QuestionList;
import org.notes.model.vo.questionList.CreateQuestionListVO;
import org.notes.service.QuestionListService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionListServiceImpl implements QuestionListService {

    private final QuestionListMapper questionListMapper;

    private final QuestionListItemMapper questionListItemMapper;

    @Override
    public QuestionList getQuestionList(Integer questionListId) {
        QuestionList questionList = questionListMapper.findById(questionListId);
        if (questionList == null) {
            throw new NotFoundException("题单不存在");
        }
        return questionList;
    }

    @Override
    public List<QuestionList> getQuestionLists() {
        return questionListMapper.findAll();
    }

    @Override
    public CreateQuestionListVO createQuestionList(CreateQuestionListBody body) {
        QuestionList questionList = new QuestionList();
        BeanUtils.copyProperties(body, questionList);

        try {
            questionListMapper.insert(questionList);
            CreateQuestionListVO createQuestionListVO = new CreateQuestionListVO();
            createQuestionListVO.setQuestionListId(questionList.getQuestionListId());

            return createQuestionListVO;
        } catch (Exception e) {
            throw new BaseException("创建题单失败");
        }
    }

    @Override
    public void deleteQuestionList(Integer questionListId) {
        QuestionList questionList = questionListMapper.findById(questionListId);

        if (questionList == null) {
            throw new NotFoundException("题单不存在");
        }

        try {
            questionListMapper.deleteById(questionListId);
            questionListItemMapper.deleteByQuestionListId(questionListId);
        } catch (Exception e) {
            throw new BaseException("删除题单失败");
        }
    }

    @Override
    public void updateQuestionList(Integer questionListId, UpdateQuestionListBody body) {
        QuestionList questionList = questionListMapper.findById(questionListId);
        if (questionList == null) {
            throw new NotFoundException("题单不存在");
        }
        BeanUtils.copyProperties(body, questionList);
        questionList.setQuestionListId(questionListId);

        try {
            questionListMapper.update(questionList);
        } catch (Exception e) {
            throw new BaseException("更新题单失败");
        }
    }
}

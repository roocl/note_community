package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import org.notes.mapper.QuestionListItemMapper;
import org.notes.mapper.QuestionListMapper;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.questionList.CreateQuestionListBody;
import org.notes.model.dto.questionList.UpdateQuestionListBody;
import org.notes.model.entity.QuestionList;
import org.notes.model.vo.questionList.CreateQuestionListVO;
import org.notes.service.QuestionListService;
import org.notes.utils.ApiResponseUtil;
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
    public ApiResponse<QuestionList> getQuestionList(Integer questionListId) {
        return ApiResponseUtil.success("获取题单成功", questionListMapper.findById(questionListId));
    }

    @Override
    public ApiResponse<List<QuestionList>> getQuestionLists() {
        return ApiResponseUtil.success("获取所有题单成功", questionListMapper.findAll());
    }

    @Override
    public ApiResponse<CreateQuestionListVO> createQuestionList(CreateQuestionListBody body) {
        QuestionList questionList = new QuestionList();
        BeanUtils.copyProperties(body, questionList);

        try {
            questionListMapper.insert(questionList);
            CreateQuestionListVO createQuestionListVO = new CreateQuestionListVO();
            createQuestionListVO.setQuestionListId(questionList.getQuestionListId());

            return ApiResponseUtil.success("创建题单成功", createQuestionListVO);
        } catch (Exception e) {
            return ApiResponseUtil.error("创建题单失败");
        }
    }

    @Override
    public ApiResponse<EmptyVO> deleteQuestionList(Integer questionListId) {
        QuestionList questionList = questionListMapper.findById(questionListId);

        if (questionList == null) {
            return ApiResponseUtil.error("题单不存在");
        }

        try {
            questionListMapper.deleteById(questionListId);
            questionListItemMapper.deleteByQuestionListId(questionListId);
            return ApiResponseUtil.success("删除题单成功");
        } catch (Exception e) {
            return ApiResponseUtil.error("删除题单失败");
        }
    }

    @Override
    public ApiResponse<EmptyVO> updateQuestionList(Integer questionListId, UpdateQuestionListBody body) {
        QuestionList questionList = questionListMapper.findById(questionListId);
        BeanUtils.copyProperties(body, questionList);
        questionList.setQuestionListId(questionListId);

        try {
            questionListMapper.update(questionList);
            return ApiResponseUtil.success("更新题单成功");
        } catch (Exception e) {
            return ApiResponseUtil.error("更新题单失败");
        }
    }
}

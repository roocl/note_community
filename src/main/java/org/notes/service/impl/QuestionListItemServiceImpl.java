package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.notes.mapper.NoteMapper;
import org.notes.mapper.QuestionListItemMapper;
import org.notes.mapper.QuestionListMapper;
import org.notes.mapper.UserMapper;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.base.Pagination;
import org.notes.model.dto.questionList.CreateQuestionListItemBody;
import org.notes.model.dto.questionList.SortQuestionListItemBody;
import org.notes.model.dto.questionListItem.QuestionListItemQueryParams;
import org.notes.model.entity.QuestionList;
import org.notes.model.entity.QuestionListItem;
import org.notes.model.vo.questionListItem.CreateQuestionListItemVO;
import org.notes.model.vo.questionListItem.QuestionListItemUserVO;
import org.notes.model.vo.questionListItem.QuestionListItemVO;
import org.notes.scope.RequestScopeData;
import org.notes.service.QuestionListItemService;
import org.notes.utils.ApiResponseUtil;
import org.notes.utils.PaginationUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Log4j2
@Service
@RequiredArgsConstructor
public class QuestionListItemServiceImpl implements QuestionListItemService {

    private final QuestionListItemMapper questionListItemMapper;

    private final QuestionListMapper questionListMapper;

    private final RequestScopeData requestScopeData;

    private final NoteMapper noteMapper;

    private final UserMapper userMapper;

    @Override
    public ApiResponse<List<QuestionListItemUserVO>> userGetQuestionListItems(QuestionListItemQueryParams queryParams) {
        int offset = PaginationUtils.calculateOffset(queryParams.getPage(), queryParams.getPageSize());
        int total = questionListItemMapper.countByQuestionListId(queryParams.getQuestionListId());
        Pagination pagination = new Pagination(queryParams.getPage(), queryParams.getPageSize(), total);

        Integer questionListId = queryParams.getQuestionListId();
        QuestionList questionList = questionListMapper.findById(questionListId);

        List<QuestionListItemVO> questionListItemVOS = questionListItemMapper.findByQuestionListIdPage(
                queryParams.getQuestionListId(),
                queryParams.getPageSize(),
                offset);

        questionListItemVOS = questionListItemVOS.stream()
                .filter(item -> item.getQuestion() != null)
                .toList();

        List<Integer> questionIds = questionListItemVOS.stream().map(
                questionListItemVO -> questionListItemVO.getQuestion().getQuestionId()).toList();

        final Set<Integer> userFinishedQuestionIds;

        if (requestScopeData.isLogin()) {
            userFinishedQuestionIds = noteMapper.filterFinishedQuestionIdsByUser(requestScopeData.getUserId(),
                    questionIds);
        } else {
            userFinishedQuestionIds = Collections.emptySet();
        }

        List<QuestionListItemUserVO> questionListItemUserVOS = questionListItemVOS.stream().map(questionListItemVO -> {
            QuestionListItemUserVO questionListItemUserVO = new QuestionListItemUserVO();
            BeanUtils.copyProperties(questionListItemVO, questionListItemUserVO);

            QuestionListItemUserVO.UserQuestionStatus userQuestionStatus = new QuestionListItemUserVO.UserQuestionStatus();
            if (requestScopeData.isLogin()) {
                userQuestionStatus.setFinished(
                        userFinishedQuestionIds.contains(questionListItemVO.getQuestion().getQuestionId()));
            } else {
                userQuestionStatus.setFinished(false);
            }

            questionListItemUserVO.setUserQuestionStatus(userQuestionStatus);

            return questionListItemUserVO;
        }).toList();

        return ApiResponseUtil.success("获取用户题单项列表成功", questionListItemUserVOS, pagination);
    }

    @Override
    public ApiResponse<List<QuestionListItemVO>> AdminGetQuestionListItems(Integer questionListId) {
        List<QuestionListItemVO> questionListItemVOS = questionListItemMapper.findByQuestionListId(questionListId);
        return ApiResponseUtil.success("获取题单项列表成功", questionListItemVOS);
    }

    @Override
    public ApiResponse<CreateQuestionListItemVO> createQuestionListItem(CreateQuestionListItemBody body) {
        QuestionListItem questionListItem = new QuestionListItem();
        BeanUtils.copyProperties(body, questionListItem);

        try {
            int rank = questionListItemMapper.nextRank(body.getQuestionListId());
            questionListItem.setRank(rank);

            questionListItemMapper.insert(questionListItem);
            CreateQuestionListItemVO createQuestionListItemVO = new CreateQuestionListItemVO();
            createQuestionListItemVO.setRank(questionListItem.getRank());
            return ApiResponseUtil.success("创建题单项成功", createQuestionListItemVO);
        } catch (Exception e) {
            return ApiResponseUtil.error("创建题单项失败");
        }
    }

    @Override
    public ApiResponse<EmptyVO> deleteQuestionListItem(Integer questionListId, Integer questionId) {
        try {
            questionListItemMapper.deleteByQuestionListIdAndQuestionId(questionListId, questionId);
            return ApiResponseUtil.success("删除题单项成功");
        } catch (Exception e) {
            return ApiResponseUtil.error("删除题单项失败");
        }
    }

    @Override
    public ApiResponse<EmptyVO> sortQuestionListItem(SortQuestionListItemBody body) {
        List<Integer> questionIds = body.getQuestionIds();
        Integer questionListId = body.getQuestionListId();

        try {
            for (int i = 0; i < questionIds.size(); i++) {
                QuestionListItem questionListItem = new QuestionListItem();
                questionListItem.setQuestionId(questionIds.get(i));
                questionListItem.setQuestionListId(questionListId);
                questionListItem.setRank(i + 1);
                questionListItemMapper.updateQuestionRank(questionListItem);
            }
            return ApiResponseUtil.success("题单项排序成功");
        } catch (Exception e) {
            return ApiResponseUtil.error("题单项排序失败");
        }
    }
}

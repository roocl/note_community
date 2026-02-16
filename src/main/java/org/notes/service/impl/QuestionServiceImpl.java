package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import org.notes.exception.BaseException;
import org.notes.exception.NotFoundException;
import org.notes.mapper.CategoryMapper;
import org.notes.mapper.NoteMapper;
import org.notes.mapper.QuestionMapper;
import org.notes.model.base.PageResult;
import org.notes.model.base.Pagination;
import org.notes.model.dto.question.*;
import org.notes.model.entity.Category;
import org.notes.model.entity.Note;
import org.notes.model.entity.Question;
import org.notes.model.vo.question.CreateQuestionVO;
import org.notes.model.vo.question.QuestionNoteVO;
import org.notes.model.vo.question.QuestionUserVO;
import org.notes.model.vo.question.QuestionVO;
import org.notes.scope.RequestScopeData;
import org.notes.service.CategoryService;
import org.notes.service.QuestionService;
import org.notes.utils.PaginationUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 题目表(Question)表服务实现类
 *
 * @author makejava
 * @since 2026-02-06 09:19:48
 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;

    private final CategoryMapper categoryMapper;

    private final RequestScopeData requestScopeData;

    private final NoteMapper noteMapper;

    private final CategoryService categoryService;

    private static final Pattern POINT_PATTERN =
            Pattern.compile("[（(]考点\\s*[：:]\\s*(.*?)[)）]");

    private static final Pattern LEVEL_PATTERN =
            Pattern.compile("【(.*?)】");

    @Override
    public Question findById(Integer questionId) {
        return questionMapper.findById(questionId);
    }

    @Override
    public Map<Integer, Question> getQuestionMapByIds(List<Integer> questionIds) {
        if (questionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Question> questions = questionMapper.findByIdBatch(questionIds);
        return questions.stream().collect(Collectors.toMap(Question::getQuestionId, question -> question));
    }

    @Override
    public QuestionNoteVO userGetQuestion(Integer questionId) {
        Question question = questionMapper.findById(questionId);
        if (question == null) {
            throw new NotFoundException("questionId非法");
        }

        QuestionNoteVO questionNoteVO = new QuestionNoteVO();
        QuestionNoteVO.UserNote userNote = new QuestionNoteVO.UserNote();

        if (requestScopeData.isLogin() && requestScopeData.getUserId() != null) {
            Note note = noteMapper.findByAuthorIdAndQuestionId(requestScopeData.getUserId(), questionId);
            if (note != null) {
                userNote.setFinished(true);
                BeanUtils.copyProperties(note, userNote);
            }
        }

        BeanUtils.copyProperties(question, questionNoteVO);
        questionNoteVO.setUserNote(userNote);

        questionMapper.incrementViewCount(questionId);

        return questionNoteVO;
    }

    @Override
    public PageResult<List<QuestionUserVO>> userGetQuestions(QuestionQueryParams queryParams) {
        int offset = PaginationUtils.calculateOffset(queryParams.getPage(), queryParams.getPageSize());
        int total = questionMapper.countByQueryParam(queryParams);
        Pagination pagination = new Pagination(queryParams.getPage(), queryParams.getPageSize(), total);

        // 根据 queryParams 查询出符合条件的问题列表
        List<Question> questions = questionMapper.findByQueryParam(queryParams, offset, queryParams.getPageSize());

        // 提取出 questionId
        List<Integer> questionIds = questions.stream().map(Question::getQuestionId).toList();

        // 存放用户完成的题目 Id 集合
        Set<Integer> userFinishedQuestionIds;

        // 如果是登录状态，则查询出当前用户完成的题目 Id 集合
        if (requestScopeData.isLogin() && requestScopeData.getUserId() != null) {
            userFinishedQuestionIds = noteMapper.filterFinishedQuestionIdsByUser(requestScopeData.getUserId(), questionIds);
        } else {
            userFinishedQuestionIds = Collections.emptySet();
        }

        List<QuestionUserVO> questionUserVOs = questions.stream().map(question -> {
            QuestionUserVO questionUserVO = new QuestionUserVO();
            QuestionUserVO.UserQuestionStatus userQuestionStatus = new QuestionUserVO.UserQuestionStatus();

            // 判断用户是否完成该道题目
            if (userFinishedQuestionIds != null && userFinishedQuestionIds.contains(question.getQuestionId())) {
                userQuestionStatus.setFinished(true);  // 用户完成了该道题目
            }

            BeanUtils.copyProperties(question, questionUserVO);

            // 设置用户完成状态
            questionUserVO.setUserQuestionStatus(userQuestionStatus);
            return questionUserVO;
        }).toList();

        return new PageResult<>(questionUserVOs, pagination);
    }

    @Override
    public List<QuestionVO> searchQuestion(SearchQuestionBody body) {
        String keyword = body.getKeyword();

        List<Question> questionList = questionMapper.findByKeyword(keyword);

        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            QuestionVO questionVO = new QuestionVO();
            BeanUtils.copyProperties(question, questionVO);
            return questionVO;
        }).toList();

        return questionVOList;
    }

    @Override
    public PageResult<List<QuestionVO>> adminGetQuestions(QuestionQueryParams queryParams) {
        int offset = PaginationUtils.calculateOffset(queryParams.getPage(), queryParams.getPageSize());
        int total = questionMapper.countByQueryParam(queryParams);

        Pagination pagination = new Pagination(queryParams.getPage(), queryParams.getPageSize(), total);
        List<Question> questions = questionMapper.findByQueryParam(queryParams, offset, queryParams.getPageSize());

        List<QuestionVO> questionVOs = questions.stream().map(question -> {
            QuestionVO questionVO = new QuestionVO();
            BeanUtils.copyProperties(question, questionVO);
            return questionVO;
        }).toList();

        return new PageResult<>(questionVOs, pagination);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateQuestionVO adminCreateQuestion(CreateQuestionBody createQuestionBody) {
        Category category = categoryMapper.findById(createQuestionBody.getCategoryId());
        if (category == null) {
            throw new NotFoundException("分类Id非法");
        }

        Question question = new Question();
        BeanUtils.copyProperties(createQuestionBody, question);

        try {
            questionMapper.insert(question);
            CreateQuestionVO createQuestionVO = new CreateQuestionVO();
            createQuestionVO.setQuestionId(question.getQuestionId());
            return createQuestionVO;
        } catch (Exception e) {
            throw new BaseException("创建问题失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateQuestion(Integer questionId, UpdateQuestionBody updateQuestionBody) {
        Question question = new Question();
        BeanUtils.copyProperties(updateQuestionBody, question);
        question.setQuestionId(questionId);

        try {
            questionMapper.update(question);
        } catch (Exception e) {
            throw new BaseException("更新问题失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDeleteQuestion(Integer questionId) {
        if (questionMapper.deleteById(questionId) > 0) {
            return;
        } else {
            throw new BaseException("删除问题失败");
        }
    }
}

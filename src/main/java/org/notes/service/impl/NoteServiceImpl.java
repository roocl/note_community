package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.notes.annotation.NeedLogin;
import org.notes.mapper.NoteLikeMapper;
import org.notes.mapper.NoteMapper;
import org.notes.mapper.QuestionMapper;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.base.Pagination;
import org.notes.model.dto.note.CreateNoteRequest;
import org.notes.model.dto.note.NoteQueryParams;
import org.notes.model.dto.note.UpdateNoteRequest;
import org.notes.model.entity.Note;
import org.notes.model.entity.Question;
import org.notes.model.entity.User;
import org.notes.model.vo.note.*;
import org.notes.scope.RequestScopeData;
import org.notes.service.*;
import org.notes.utils.ApiResponseUtil;
import org.notes.utils.PaginationUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteMapper noteMapper;

    private final UserService userService;

    private final QuestionService questionService;

    private final NoteLikeService noteLikeService;

    private final CollectionNoteService collectionNoteService;

    private final RequestScopeData requestScopeData;

    private final CategoryService categoryService;

    private final QuestionMapper questionMapper;

    @Override
    public ApiResponse<List<NoteVO>> getNotes(NoteQueryParams params) {
        int offset = PaginationUtils.calculateOffset(params.getPage(), params.getPageSize());
        int total = noteMapper.countNotesByQueryParam(params);
        Pagination pagination = new Pagination(params.getPage(), params.getPageSize(), total);

        List<Note> notes = noteMapper.findByQueryParams(params, offset, params.getPageSize());

        List<Integer> noteIds = notes.stream().map(Note::getNoteId).toList();
        List<Long> authorIds = notes.stream().map(Note::getAuthorId).toList();
        List<Integer> questionIds = notes.stream().map(Note::getQuestionId).toList();

        Map<Long, User> userMap = userService.getUserMapByIds(authorIds);
        Map<Integer, Question> questionMap = questionService.getQuestionMapByIds(questionIds);

        Set<Integer> userLikedNoteIds;
        Set<Integer> userCollectedNoteIds;

        if (requestScopeData.isLogin() && requestScopeData.getUserId() != null) {
            Long userId = requestScopeData.getUserId();
            userLikedNoteIds = noteLikeService.findUserLikedNoteIds(userId, noteIds);
            userCollectedNoteIds = collectionNoteService.findUserCollectedNoteIds(userId, noteIds);
        } else {
            userLikedNoteIds = Collections.emptySet();
            userCollectedNoteIds = Collections.emptySet();
        }

        try {
            List<NoteVO> noteVOs = notes.stream().map(note -> {
                NoteVO noteVO = new NoteVO();
                BeanUtils.copyProperties(note, noteVO);

                User author = userMap.get(note.getAuthorId());
                if (author != null) {
                    NoteVO.SimpleAuthorVO simpleAuthorVO = new NoteVO.SimpleAuthorVO();
                    BeanUtils.copyProperties(author, simpleAuthorVO);
                    noteVO.setAuthor(simpleAuthorVO);
                }

                Question question = questionMap.get(note.getQuestionId());
                if (question != null) {
                    NoteVO.SimpleQuestionVO simpleQuestionVO = new NoteVO.SimpleQuestionVO();
                    BeanUtils.copyProperties(question, simpleQuestionVO);
                    noteVO.setQuestion(simpleQuestionVO);
                }

                NoteVO.UserActionsVO userActionsVO = new NoteVO.UserActionsVO();
                if (userLikedNoteIds != null && userLikedNoteIds.contains(note.getNoteId())) {
                    userActionsVO.setIsLiked(true);
                }
                if (userCollectedNoteIds != null && userCollectedNoteIds.contains(note.getNoteId())) {
                    userActionsVO.setIsCollected(true);
                }

                //todo markdown折叠

                noteVO.setUserActions(userActionsVO);
                return noteVO;
            }).toList();

            return ApiResponseUtil.success("获取笔记列表成功", noteVOs, pagination);
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            return ApiResponseUtil.error("获取笔记列表失败");
        }
    }

    @Override
    @NeedLogin
    public ApiResponse<CreateNoteVO> createNote(CreateNoteRequest request) {
        Integer questionId = request.getQuestionId();
        Question question = questionService.findById(questionId);

        if (question == null) {
            return ApiResponseUtil.error("questionId对应的问题不存在");
        }

        Long userId = requestScopeData.getUserId();

        Note note = new Note();
        BeanUtils.copyProperties(request, note);
        note.setAuthorId(userId);

        try {
            noteMapper.insert(note);
            CreateNoteVO createNoteVO = new CreateNoteVO();
            return ApiResponseUtil.success("创建笔记成功", createNoteVO);
        } catch (Exception e) {
            return ApiResponseUtil.error("创建笔记失败");
        }
    }

    @Override
    @NeedLogin
    public ApiResponse<EmptyVO> updateNote(Integer noteId, UpdateNoteRequest request) {
        Note note = noteMapper.findById(noteId);
        if (note == null) {
            return ApiResponseUtil.error("noteId对应的笔记不存在");
        }

        Long userId = requestScopeData.getUserId();
        if (!Objects.equals(userId, note.getAuthorId())) {
            return ApiResponseUtil.error("没有权限修改别人的笔记");
        }

        try {
            note.setContent(request.getContent());
            noteMapper.update(note);
            return ApiResponseUtil.success("更新笔记成功");
        } catch (Exception e) {
            return ApiResponseUtil.error("更新笔记失败");
        }
    }

    @Override
    @NeedLogin
    public ApiResponse<EmptyVO> deleteNote(Integer noteId) {
        Note note = noteMapper.findById(noteId);
        if (note == null) {
            return ApiResponseUtil.error("noteId对应的笔记不存在");
        }

        Long userId = requestScopeData.getUserId();
        if (!Objects.equals(userId, note.getAuthorId())) {
            return ApiResponseUtil.error("没有权限删除别人的笔记");
        }

        try {
            noteMapper.deleteById(noteId);
            return ApiResponseUtil.success("删除笔记成功");
        } catch (Exception e) {
            return ApiResponseUtil.error("删除笔记失败");
        }
    }

    @Override
    public ApiResponse<List<NoteRankListItem>> submitNoteRank() {
        return ApiResponseUtil.success("获取笔记排行榜成功", noteMapper.submitNoteRank());
    }

    @Override
    public ApiResponse<List<NoteHeatMapItem>> submitNoteHeatMap() {
        Long userId = requestScopeData.getUserId();
        return ApiResponseUtil.success("获取笔记热力图成功", noteMapper.submitNoteHeatMap(userId));
    }

    @Override
    public ApiResponse<Top3Count> submitNoteTop3Count() {
        Long userId = requestScopeData.getUserId();
        Top3Count top3Count = noteMapper.submitNoteTop3Count(userId);
        return ApiResponseUtil.success("获取笔记top3成功", top3Count);
    }
}

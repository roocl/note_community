package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.notes.annotation.NeedLogin;
import org.notes.exception.BaseException;
import org.notes.exception.ForbiddenException;
import org.notes.exception.NotFoundException;
import org.notes.mapper.NoteLikeMapper;
import org.notes.mapper.NoteMapper;
import org.notes.mapper.QuestionMapper;
import org.notes.model.base.PageResult;
import org.notes.model.base.Pagination;
import org.notes.model.dto.note.CreateNoteRequest;
import org.notes.model.dto.note.NoteQueryParams;
import org.notes.model.dto.note.UpdateNoteRequest;
import org.notes.model.entity.Note;
import org.notes.model.entity.Question;
import org.notes.model.entity.User;
import org.notes.model.es.NoteDocument;
import org.notes.model.vo.note.*;
import org.notes.repository.NoteSearchRepository;
import org.notes.scope.RequestScopeData;
import org.notes.service.*;
import org.notes.utils.PaginationUtils;
import org.notes.utils.SearchUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final NoteSearchRepository noteSearchRepository;

    @Override
    public PageResult<List<NoteVO>> getNotes(NoteQueryParams params) {
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

                // todo markdown折叠

                noteVO.setUserActionsVO(userActionsVO);
                return noteVO;
            }).toList();

            return new PageResult<>(noteVOs, pagination);
        } catch (Exception e) {
            throw new BaseException("获取笔记列表失败");
        }
    }

    @Override
    @NeedLogin
    @Transactional(rollbackFor = Exception.class)
    public CreateNoteVO createNote(CreateNoteRequest request) {
        Integer questionId = request.getQuestionId();
        Question question = questionService.findById(questionId);

        if (question == null) {
            throw new NotFoundException("questionId对应的问题不存在");
        }

        Long userId = requestScopeData.getUserId();

        Note note = new Note();
        BeanUtils.copyProperties(request, note);
        note.setAuthorId(userId);
        String processedVector = SearchUtils.preprocessKeyword(request.getContent());
        note.setSearchVector(processedVector);

        try {
            noteMapper.insert(note);

            // 同步到 Elasticsearch
            syncNoteToEs(note);

            CreateNoteVO createNoteVO = new CreateNoteVO();
            return createNoteVO;
        } catch (Exception e) {
            throw new BaseException("创建笔记失败");
        }
    }

    @Override
    @NeedLogin
    @Transactional(rollbackFor = Exception.class)
    public void updateNote(Integer noteId, UpdateNoteRequest request) {
        Note note = noteMapper.findById(noteId);
        if (note == null) {
            throw new NotFoundException("noteId对应的笔记不存在");
        }

        Long userId = requestScopeData.getUserId();
        if (!Objects.equals(userId, note.getAuthorId())) {
            throw new ForbiddenException("没有权限修改别人的笔记");
        }

        try {
            String content = request.getContent();
            String processedVector = SearchUtils.preprocessKeyword(content);
            note.setContent(content);
            note.setSearchVector(processedVector);

            noteMapper.update(note);

            // 同步到 Elasticsearch
            syncNoteToEs(note);
        } catch (Exception e) {
            throw new BaseException("更新笔记失败");
        }
    }

    @Override
    @NeedLogin
    @Transactional(rollbackFor = Exception.class)
    public void deleteNote(Integer noteId) {
        Note note = noteMapper.findById(noteId);
        if (note == null) {
            throw new NotFoundException("noteId对应的笔记不存在");
        }

        Long userId = requestScopeData.getUserId();
        if (!Objects.equals(userId, note.getAuthorId())) {
            throw new ForbiddenException("没有权限删除别人的笔记");
        }

        try {
            noteMapper.deleteById(noteId);

            // 从 Elasticsearch 删除
            try {
                noteSearchRepository.deleteById(noteId);
            } catch (Exception esEx) {
                log.warn("删除笔记ES索引失败，noteId={}", noteId, esEx);
            }
        } catch (Exception e) {
            throw new BaseException("删除笔记失败");
        }
    }

    @Override
    public List<NoteRankListItem> submitNoteRank() {
        return noteMapper.submitNoteRank();
    }

    @Override
    public List<NoteHeatMapItem> submitNoteHeatMap() {
        Long userId = requestScopeData.getUserId();
        return noteMapper.submitNoteHeatMap(userId);
    }

    @Override
    public Top3Count submitNoteTop3Count() {
        Long userId = requestScopeData.getUserId();
        return noteMapper.submitNoteTop3Count(userId);
    }

    /**
     * 同步笔记到 Elasticsearch
     */
    private void syncNoteToEs(Note note) {
        try {
            NoteDocument doc = new NoteDocument();
            BeanUtils.copyProperties(note, doc);
            noteSearchRepository.save(doc);
        } catch (Exception e) {
            log.warn("同步笔记到ES失败，noteId={}", note.getNoteId(), e);
        }
    }
}

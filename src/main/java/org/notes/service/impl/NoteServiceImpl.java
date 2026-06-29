package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.notes.annotation.NeedLogin;
import org.notes.exception.BaseException;
import org.notes.exception.ForbiddenException;
import org.notes.exception.NotFoundException;
import org.notes.mapper.NoteMapper;
import org.notes.model.base.PageResult;
import org.notes.model.base.Pagination;
import org.notes.model.dto.note.CreateNoteRequest;
import org.notes.model.dto.note.NoteQueryParams;
import org.notes.model.dto.note.UpdateNoteRequest;
import org.notes.model.entity.Category;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    private final NoteSearchRepository noteSearchRepository;

    private final StringRedisTemplate stringRedisTemplate;

    private final EsSyncFailureService esSyncFailureService;

    private final RedisProtectionService redisProtectionService;

    @Override
    @Cacheable(value = "notes", key = "'list:' + T(java.util.Objects).hash(#params.page, #params.pageSize, #params.questionId, #params.authorId, #params.collectionId, #params.sortBy, #params.sortOrder, #params.recentDays)", unless = "#result == null")
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
            throw new BaseException("获取笔记列表失败", e);
        }
    }

    @Override
    @NeedLogin
    @CacheEvict(value = "notes", allEntries = true)
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

            // 维护今日提交排行榜（事务提交后执行）
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    String rankKey = "note:rank:submit:" + LocalDate.now();
                    stringRedisTemplate.opsForZSet().incrementScore(rankKey, String.valueOf(userId), 1);
                    // Set TTL on first write of the day (2 days to cover "yesterday" view)
                    stringRedisTemplate.expire(rankKey, redisProtectionService.withJitter(Duration.ofDays(2), 300));
                }
            });

            // 同步到 Elasticsearch
            syncNoteToEs(note);

            CreateNoteVO createNoteVO = new CreateNoteVO();
            return createNoteVO;
        } catch (Exception e) {
            throw new BaseException("创建笔记失败", e);
        }
    }

    @Override
    @NeedLogin
    @CacheEvict(value = "notes", allEntries = true)
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
            throw new BaseException("更新笔记失败", e);
        }
    }

    @Override
    @NeedLogin
    @CacheEvict(value = "notes", allEntries = true)
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
                esSyncFailureService.recordFailure(
                        EsSyncFailureServiceImpl.ENTITY_NOTE,
                        Long.valueOf(noteId),
                        EsSyncFailureServiceImpl.OP_DELETE,
                        esEx);
            }

            Long authorId = note.getAuthorId();
            // 维护今日提交排行榜（事务提交后扣减）
            try {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        String rankKey = "note:rank:submit:" + LocalDate.now();
                        stringRedisTemplate.opsForZSet().incrementScore(rankKey, String.valueOf(authorId), -1);
                    }
                });
            } catch (Exception redisEx) {
                log.warn("注册Redis排行扣减回调失败，noteId={}", noteId, redisEx);
            }
        } catch (Exception e) {
            throw new BaseException("删除笔记失败", e);
        }
    }

    @Override
    public List<NoteRankListItem> submitNoteRank() {
        String rankKey = "note:rank:submit:" + LocalDate.now();
        Set<ZSetOperations.TypedTuple<String>> topSet =
                stringRedisTemplate.opsForZSet().reverseRangeWithScores(rankKey, 0, 9);

        if (topSet == null || topSet.isEmpty()) {
            return rebuildSubmitNoteRank(rankKey);
        }

        List<Long> userIds = topSet.stream()
                .map(t -> Long.valueOf(t.getValue()))
                .toList();
        Map<Long, User> userMap = userService.getUserMapByIds(userIds);

        List<NoteRankListItem> result = new ArrayList<>();
        int rank = 1;
        for (ZSetOperations.TypedTuple<String> t : topSet) {
            Long uid = Long.valueOf(t.getValue());
            User u = userMap.get(uid);
            if (u == null) continue;
            NoteRankListItem item = new NoteRankListItem();
            item.setUserId(uid);
            item.setUsername(u.getUsername());
            item.setAvatarUrl(u.getAvatarUrl());
            item.setNoteCount(t.getScore().intValue());
            item.setRank(rank++);
            result.add(item);
        }
        return result;
    }

    private List<NoteRankListItem> rebuildSubmitNoteRank(String rankKey) {
        String lockKey = rankKey + ":rebuild-lock";
        String token = redisProtectionService.tryLock(lockKey, Duration.ofSeconds(5));
        List<NoteRankListItem> rankItems = noteMapper.submitNoteRank();
        if (token == null) {
            return rankItems;
        }

        try {
            if (!rankItems.isEmpty()) {
                for (NoteRankListItem item : rankItems) {
                    if (item.getUserId() != null && item.getNoteCount() != null) {
                        stringRedisTemplate.opsForZSet()
                                .add(rankKey, String.valueOf(item.getUserId()), item.getNoteCount());
                    }
                }
                stringRedisTemplate.expire(rankKey, redisProtectionService.withJitter(Duration.ofMinutes(30), 300));
            }
            return rankItems;
        } catch (Exception e) {
            log.warn("Redis排行榜重建失败，key={}", rankKey, e);
            return rankItems;
        } finally {
            redisProtectionService.unlock(lockKey, token);
        }
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

            // 从关联 Question 获取标题和分类名
            Integer questionId = note.getQuestionId();
            if (questionId != null) {
                Question question = questionService.findById(questionId);
                if (question != null) {
                    if (question.getTitle() != null) {
                        doc.setTitle(question.getTitle());
                        doc.setSuggest(new Completion(new String[]{question.getTitle()}));
                    }
                    if (question.getCategoryId() != null) {
                        Category category = categoryService.findById(question.getCategoryId());
                        if (category != null) {
                            doc.setCategoryName(category.getName());
                        }
                    }
                }
            }

            noteSearchRepository.save(doc);
        } catch (Exception e) {
            log.warn("同步笔记到ES失败，noteId={}", note.getNoteId(), e);
            if (note.getNoteId() != null) {
                esSyncFailureService.recordFailure(
                        EsSyncFailureServiceImpl.ENTITY_NOTE,
                        Long.valueOf(note.getNoteId()),
                        EsSyncFailureServiceImpl.OP_SAVE,
                        e);
            }
        }
    }
}

package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.notes.exception.BadRequestException;
import org.notes.exception.NotFoundException;
import org.notes.mapper.EsSyncFailureMapper;
import org.notes.mapper.NoteMapper;
import org.notes.mapper.UserMapper;
import org.notes.model.entity.Category;
import org.notes.model.entity.EsSyncFailure;
import org.notes.model.entity.Note;
import org.notes.model.entity.Question;
import org.notes.model.entity.User;
import org.notes.model.es.NoteDocument;
import org.notes.model.es.UserDocument;
import org.notes.repository.NoteSearchRepository;
import org.notes.repository.UserSearchRepository;
import org.notes.service.CategoryService;
import org.notes.service.EsSyncFailureService;
import org.notes.service.QuestionService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class EsSyncFailureServiceImpl implements EsSyncFailureService {

    public static final String ENTITY_NOTE = "NOTE";
    public static final String ENTITY_USER = "USER";
    public static final String OP_SAVE = "SAVE";
    public static final String OP_DELETE = "DELETE";

    private final EsSyncFailureMapper esSyncFailureMapper;
    private final NoteMapper noteMapper;
    private final UserMapper userMapper;
    private final NoteSearchRepository noteSearchRepository;
    private final UserSearchRepository userSearchRepository;
    private final QuestionService questionService;
    private final CategoryService categoryService;

    @Override
    public void recordFailure(String entityType, Long entityId, String operation, Exception exception) {
        if (entityId == null) {
            return;
        }
        String errorMessage = exception.getMessage() == null ? exception.toString() : exception.getMessage();
        EsSyncFailure openFailure = esSyncFailureMapper.findOpen(entityType, entityId, operation);
        if (openFailure != null) {
            int retryCount = openFailure.getRetryCount() == null ? 0 : openFailure.getRetryCount();
            esSyncFailureMapper.markFailed(openFailure.getId(), errorMessage, retryCount);
            return;
        }

        EsSyncFailure failure = new EsSyncFailure();
        failure.setEntityType(entityType);
        failure.setEntityId(entityId);
        failure.setOperation(operation);
        failure.setStatus("PENDING");
        failure.setRetryCount(0);
        failure.setErrorMessage(errorMessage);
        esSyncFailureMapper.insert(failure);
    }

    @Override
    public List<EsSyncFailure> listFailures() {
        return esSyncFailureMapper.findAll();
    }

    @Override
    public EsSyncFailure getFailure(Long id) {
        EsSyncFailure failure = esSyncFailureMapper.findById(id);
        if (failure == null) {
            throw new NotFoundException("ES同步失败任务不存在");
        }
        return failure;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryFailure(Long id) {
        EsSyncFailure failure = getFailure(id);
        int nextRetryCount = failure.getRetryCount() == null ? 1 : failure.getRetryCount() + 1;
        try {
            retry(failure);
            esSyncFailureMapper.markSuccess(id);
        } catch (Exception e) {
            log.warn("ES同步失败任务重试失败，id={}", id, e);
            esSyncFailureMapper.markFailed(id, e.getMessage() == null ? e.toString() : e.getMessage(), nextRetryCount);
        }
    }

    @Override
    public void retryPendingFailures(int limit) {
        List<EsSyncFailure> failures = esSyncFailureMapper.findPending(limit);
        for (EsSyncFailure failure : failures) {
            retryFailure(failure.getId());
        }
    }

    @Override
    public int reconcileAll() {
        return reconcileNotes() + reconcileUsers();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reconcileNotes() {
        int mismatchCount = 0;
        for (Note note : noteMapper.findAll()) {
            try {
                NoteDocument doc = noteSearchRepository.findById(note.getNoteId()).orElse(null);
                if (isNoteMismatch(note, doc)) {
                    recordFailure(ENTITY_NOTE, Long.valueOf(note.getNoteId()), OP_SAVE,
                            new IllegalStateException("ES笔记文档缺失或不一致"));
                    mismatchCount++;
                }
            } catch (Exception e) {
                recordFailure(ENTITY_NOTE, Long.valueOf(note.getNoteId()), OP_SAVE, e);
                mismatchCount++;
            }
        }

        try {
            for (NoteDocument doc : noteSearchRepository.findAll()) {
                if (doc.getNoteId() != null && noteMapper.findById(doc.getNoteId()) == null) {
                    recordFailure(ENTITY_NOTE, Long.valueOf(doc.getNoteId()), OP_DELETE,
                            new IllegalStateException("ES笔记文档不存在于数据库中"));
                    mismatchCount++;
                }
            }
        } catch (Exception e) {
            log.warn("ES笔记孤儿文档对账失败", e);
        }
        return mismatchCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reconcileUsers() {
        int mismatchCount = 0;
        for (User user : userMapper.findAll()) {
            try {
                UserDocument doc = userSearchRepository.findById(user.getUserId()).orElse(null);
                if (isUserMismatch(user, doc)) {
                    recordFailure(ENTITY_USER, user.getUserId(), OP_SAVE,
                            new IllegalStateException("ES用户文档缺失或不一致"));
                    mismatchCount++;
                }
            } catch (Exception e) {
                recordFailure(ENTITY_USER, user.getUserId(), OP_SAVE, e);
                mismatchCount++;
            }
        }

        try {
            for (UserDocument doc : userSearchRepository.findAll()) {
                if (doc.getUserId() != null && userMapper.findById(doc.getUserId()) == null) {
                    recordFailure(ENTITY_USER, doc.getUserId(), OP_DELETE,
                            new IllegalStateException("ES用户文档不存在于数据库中"));
                    mismatchCount++;
                }
            }
        } catch (Exception e) {
            log.warn("ES用户孤儿文档对账失败", e);
        }
        return mismatchCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFailure(Long id) {
        esSyncFailureMapper.deleteById(id);
    }

    private void retry(EsSyncFailure failure) {
        if (ENTITY_NOTE.equals(failure.getEntityType())) {
            retryNote(failure);
            return;
        }
        if (ENTITY_USER.equals(failure.getEntityType())) {
            retryUser(failure);
            return;
        }
        throw new BadRequestException("不支持的ES同步实体类型");
    }

    private void retryNote(EsSyncFailure failure) {
        Integer noteId = failure.getEntityId().intValue();
        if (OP_DELETE.equals(failure.getOperation())) {
            noteSearchRepository.deleteById(noteId);
            return;
        }
        Note note = noteMapper.findById(noteId);
        if (note == null) {
            noteSearchRepository.deleteById(noteId);
            return;
        }
        noteSearchRepository.save(toNoteDocument(note));
    }

    private void retryUser(EsSyncFailure failure) {
        Long userId = failure.getEntityId();
        if (OP_DELETE.equals(failure.getOperation())) {
            userSearchRepository.deleteById(userId);
            return;
        }
        User user = userMapper.findById(userId);
        if (user == null) {
            userSearchRepository.deleteById(userId);
            return;
        }
        UserDocument doc = new UserDocument();
        BeanUtils.copyProperties(user, doc);
        userSearchRepository.save(doc);
    }

    private NoteDocument toNoteDocument(Note note) {
        NoteDocument doc = new NoteDocument();
        BeanUtils.copyProperties(note, doc);

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
        return doc;
    }

    private boolean isNoteMismatch(Note note, NoteDocument doc) {
        return doc == null
                || !Objects.equals(note.getAuthorId(), doc.getAuthorId())
                || !Objects.equals(note.getQuestionId(), doc.getQuestionId())
                || !Objects.equals(note.getContent(), doc.getContent())
                || !Objects.equals(note.getLikeCount(), doc.getLikeCount())
                || !Objects.equals(note.getCommentCount(), doc.getCommentCount())
                || !Objects.equals(note.getCollectCount(), doc.getCollectCount());
    }

    private boolean isUserMismatch(User user, UserDocument doc) {
        return doc == null
                || !Objects.equals(user.getUsername(), doc.getUsername())
                || !Objects.equals(user.getAccount(), doc.getAccount())
                || !Objects.equals(user.getEmail(), doc.getEmail())
                || !Objects.equals(user.getAvatarUrl(), doc.getAvatarUrl())
                || !Objects.equals(user.getSchool(), doc.getSchool())
                || !Objects.equals(user.getSignature(), doc.getSignature());
    }
}

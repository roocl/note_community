package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import org.notes.annotation.NeedLogin;
import org.notes.exception.BadRequestException;
import org.notes.exception.BaseException;
import org.notes.exception.ForbiddenException;
import org.notes.exception.NotFoundException;
import org.notes.mapper.CollectionMapper;
import org.notes.mapper.CollectionNoteMapper;
import org.notes.mapper.NoteMapper;
import org.notes.model.dto.collectionNote.UpdateCollectionNoteBatchBody;
import org.notes.model.dto.collectionNote.UpdateCollectionNoteBody;
import org.notes.model.dto.collectionNote.UpdateCollectionNoteBatchBody.Action;
import org.notes.model.entity.Collection;
import org.notes.model.entity.CollectionNote;
import org.notes.model.entity.Note;
import org.notes.model.vo.note.NoteVO;
import org.notes.scope.RequestScopeData;
import org.notes.service.CollectionNoteService;
import org.notes.service.NoteListCache;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CollectionNoteServiceImpl implements CollectionNoteService {

    private final CollectionMapper collectionMapper;

    private final CollectionNoteMapper collectionNoteMapper;

    private final NoteMapper noteMapper;

    private final RequestScopeData requestScopeData;

    private final NoteListCache noteListCache;

    @Override
    @NeedLogin
    public List<NoteVO> getCollectNotes(Integer collectionId) {
        Long creatorId = requestScopeData.getUserId();
        Collection collection = collectionMapper.findByIdAndCreatorId(collectionId, creatorId);

        if (collection == null) {
            throw new ForbiddenException("收藏夹不存在或者没有权限查看");
        }
        List<Integer> noteIds = collectionNoteMapper.findNoteIdsByCollectionId(collectionId);

        if (noteIds.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            List<Note> notes = noteMapper.findByIds(noteIds);
            List<NoteVO> noteVOS = notes.stream().map(note -> {
                NoteVO noteVO = new NoteVO();
                BeanUtils.copyProperties(note, noteVO);
                return noteVO;
            }).toList();
            return noteVOS;
        } catch (Exception e) {
            throw new BaseException("查询收藏夹笔记失败", e);
        }
    }

    @Override
    @NeedLogin
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void createCollectionNote(Integer collectionId, UpdateCollectionNoteBody requestBody) {
        changeCollections(requestBody.getNoteId(), Map.of(collectionId, Action.CREATE));
    }

    @Override
    @NeedLogin
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void deleteCollectionNote(Integer collectionId, UpdateCollectionNoteBody requestBody) {
        changeCollections(requestBody.getNoteId(), Map.of(collectionId, Action.DELETE));
    }

    @Override
    @NeedLogin
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void batchModifyCollection(UpdateCollectionNoteBatchBody requestBody) {
        Map<Integer, Action> changes = new TreeMap<>();
        for (UpdateCollectionNoteBatchBody.UpdateItem item : requestBody.getCollections()) {
            if (item.getCollectionId() == null || item.getAction() == null) {
                throw new BadRequestException("收藏夹操作非法");
            }
            Action previous = changes.putIfAbsent(item.getCollectionId(), item.getAction());
            if (previous != null && previous != item.getAction()) {
                throw new BadRequestException("同一收藏夹不能包含相反操作");
            }
        }
        changeCollections(requestBody.getNoteId(), changes);
    }

    private void changeCollections(Integer noteId, Map<Integer, Action> changes) {
        Long userId = requestScopeData.getUserId();
        for (Integer collectionId : new TreeSet<>(changes.keySet())) {
            if (collectionMapper.findByIdAndCreatorIdForUpdate(collectionId, userId) == null) {
                throw new ForbiddenException("收藏夹不存在或者没有权限修改");
            }
        }
        if (noteMapper.findByIdForUpdate(noteId) == null) {
            throw new NotFoundException("笔记不存在");
        }
        boolean changed = false;
        for (Map.Entry<Integer, Action> change : changes.entrySet()) {
            Integer collectionId = change.getKey();
            if (change.getValue() == Action.CREATE) {
                if (collectionNoteMapper.findByCollectionIdAndNoteIdForUpdate(collectionId, noteId) == null) {
                    CollectionNote relation = new CollectionNote();
                    relation.setCollectionId(collectionId);
                    relation.setNoteId(noteId);
                    changed |= collectionNoteMapper.insert(relation) > 0;
                }
            } else {
                changed |= collectionNoteMapper.deleteByCollectionIdAndNoteId(collectionId, noteId) > 0;
            }
        }
        if (changed) {
            noteMapper.refreshCollectCount(noteId);
            noteListCache.invalidateAfterCommit();
        }
    }

    @Override
    @NeedLogin
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void deleteCollection(Integer collectionId) {
        Long userId = requestScopeData.getUserId();
        if (collectionMapper.findByIdAndCreatorIdForUpdate(collectionId, userId) == null) {
            throw new ForbiddenException("收藏夹不存在或者没有权限删除");
        }
        List<Integer> noteIds = collectionNoteMapper.findNoteIdsByCollectionId(collectionId);
        for (Integer noteId : new TreeSet<>(noteIds)) {
            noteMapper.findByIdForUpdate(noteId);
        }
        collectionNoteMapper.deleteByCollectionId(collectionId);
        collectionMapper.deleteById(collectionId);
        for (Integer noteId : noteIds) {
            noteMapper.refreshCollectCount(noteId);
        }
        noteListCache.invalidateAfterCommit();
    }

    @Override
    public Set<Integer> findUserCollectedNoteIds(Long userId, List<Integer> noteIds) {
        List<Integer> userCollectedNoteIds = collectionNoteMapper.findUserCollectedNoteIds(userId, noteIds);
        return new HashSet<>(userCollectedNoteIds);
    }
}

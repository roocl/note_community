package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import org.notes.annotation.NeedLogin;
import org.notes.mapper.CollectionMapper;
import org.notes.mapper.CollectionNoteMapper;
import org.notes.mapper.NoteMapper;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.collectionNote.UpdateCollectionNoteBatchBody;
import org.notes.model.dto.collectionNote.UpdateCollectionNoteBody;
import org.notes.model.entity.Collection;
import org.notes.model.entity.CollectionNote;
import org.notes.model.entity.Note;
import org.notes.model.vo.note.NoteVO;
import org.notes.scope.RequestScopeData;
import org.notes.service.CollectionNoteService;
import org.notes.utils.ApiResponseUtil;
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

    @Override
    @NeedLogin
    public ApiResponse<List<NoteVO>> getCollectNotes(Integer collectionId) {
        Long creatorId = requestScopeData.getUserId();
        Collection collection = collectionMapper.findByIdAndCreatorId(collectionId, creatorId);

        if (collection == null) {
            return ApiResponseUtil.error("收藏夹不存在或者没有权限查看");
        }
        List<Integer> noteIds = collectionNoteMapper.findNoteIdsByCollectionId(collectionId);

        if (noteIds.isEmpty()) {
            return ApiResponseUtil.success("查询笔记成功", new ArrayList<>());
        }

        try {
            List<Note> notes = noteMapper.findByIds(noteIds);
            List<NoteVO> noteVOS = notes.stream().map(note -> {
                NoteVO noteVO = new NoteVO();
                BeanUtils.copyProperties(note, noteVO);
                return noteVO;
            }).toList();
            return ApiResponseUtil.success("查询收藏夹笔记成功", noteVOS);
        } catch (Exception e) {
            return ApiResponseUtil.error("查询收藏夹笔记失败");
        }
    }

    @Override
    @NeedLogin
    @Transactional
    public ApiResponse<EmptyVO> createCollectionNote(Integer collectionId, UpdateCollectionNoteBody requestBody) {
        Long creatorId = requestScopeData.getUserId();
        Collection collection = collectionMapper.findByIdAndCreatorId(collectionId, creatorId);
        if (collection == null) {
            return ApiResponseUtil.error("收藏夹不存在或者没有权限添加笔记");
        }

        Integer noteId = requestBody.getNoteId();
        Note note = noteMapper.findById(noteId);
        if (note == null) {
            return ApiResponseUtil.error("笔记不存在");
        }

        CollectionNote collectionNote = collectionNoteMapper.findByCollectionIdAndNoteId(collectionId, noteId);

        if (collectionNote != null) {
            return ApiResponseUtil.error("该收藏夹已存在目标笔记，请勿重复添加");
        }

        try {
            collectionNote = new CollectionNote();
            collectionNote.setCollectionId(collectionId);
            collectionNote.setNoteId(noteId);

            collectionNoteMapper.insert(collectionNote);

            // 如果该笔记之前未被当前用户收藏过，增加笔记的收藏计数
            if (collectionMapper.countByCreatorIdAndNoteId(creatorId, noteId) == 1) {
                noteMapper.collectNote(noteId);
            }

            return ApiResponseUtil.success("添加收藏夹笔记成功");
        } catch (Exception e) {
            return ApiResponseUtil.error("添加收藏夹笔记失败");
        }
    }

    @Override
    @NeedLogin
    @Transactional
    public ApiResponse<EmptyVO> deleteCollectionNote(Integer collectionId, UpdateCollectionNoteBody requestBody) {
        Long creatorId = requestScopeData.getUserId();
        Collection collection = collectionMapper.findByIdAndCreatorId(collectionId, creatorId);
        if (collection == null) {
            return ApiResponseUtil.error("收藏夹不存在或者没有权限删除笔记");
        }

        Integer noteId = requestBody.getNoteId();
        Note note = noteMapper.findById(noteId);
        if (note == null) {
            return ApiResponseUtil.error("笔记不存在");
        }

        CollectionNote collectionNote = collectionNoteMapper.findByCollectionIdAndNoteId(collectionId, noteId);
        if (collectionNote == null) {
            return ApiResponseUtil.error("该收藏夹不存在目标笔记");
        }

        try {
            collectionNoteMapper.delete(collectionNote);

            // 如果当前用户已无收藏夹包含该笔记，减少笔记的收藏计数
            if (collectionMapper.countByCreatorIdAndNoteId(creatorId, noteId) == 0) {
                noteMapper.unCollectNote(noteId);
            }

            return ApiResponseUtil.success("删除收藏夹笔记成功");
        } catch (Exception e) {
            return ApiResponseUtil.error("删除收藏夹笔记失败");
        }
    }

    @Override
    @NeedLogin
    @Transactional
    public ApiResponse<EmptyVO> batchModifyCollection(UpdateCollectionNoteBatchBody requestBody) {
        Long creatorId = requestScopeData.getUserId();
        Integer noteId = requestBody.getNoteId();

        UpdateCollectionNoteBatchBody.UpdateItem[] collections = requestBody.getCollections();

        for (UpdateCollectionNoteBatchBody.UpdateItem collection : collections) {
            Integer collectionId = collection.getCollectionId();
            String action = collection.getAction();

            Collection collectionEntity = collectionMapper.findByIdAndCreatorId(collectionId, creatorId);

            if (collectionEntity == null) {
                return ApiResponseUtil.error("收藏夹不存在或者没有权限修改");
            }

            if (Objects.equals(action, "create")) {
                try {
                    if (collectionMapper.countByCreatorIdAndNoteId(creatorId, noteId) == 0) {
                        noteMapper.collectNote(noteId);
                    }

                    CollectionNote collectionNote = new CollectionNote();
                    collectionNote.setCollectionId(collectionId);
                    collectionNote.setNoteId(noteId);
                    collectionNoteMapper.insert(collectionNote);
                } catch (Exception e) {
                    return ApiResponseUtil.error("收藏失败");
                }
            } else if (Objects.equals(action, "delete")) {
                try {
                    collectionNoteMapper.deleteByCollectionIdAndNoteId(collectionId, noteId);
                    if (collectionMapper.countByCreatorIdAndNoteId(creatorId, noteId) == 0) {
                        noteMapper.unCollectNote(noteId);
                    }
                } catch (Exception e) {
                    return ApiResponseUtil.error("取消收藏失败");
                }
            }
        }
        return ApiResponseUtil.success("操作成功");
    }

    @Override
    public Set<Integer> findUserCollectedNoteIds(Long userId, List<Integer> noteIds) {
        List<Integer> userCollectedNoteIds = collectionNoteMapper.findUserCollectedNoteIds(userId, noteIds);
        return new HashSet<>(userCollectedNoteIds);
    }
}

package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import org.notes.annotation.NeedLogin;
import org.notes.exception.BaseException;
import org.notes.exception.ForbiddenException;
import org.notes.mapper.CollectionMapper;
import org.notes.mapper.CollectionNoteMapper;
import org.notes.mapper.NoteMapper;
import org.notes.model.dto.collection.CollectionQueryParams;
import org.notes.model.dto.collection.CreateCollectionBody;
import org.notes.model.dto.collection.UpdateCollectionBody;
import org.notes.model.entity.Collection;
import org.notes.model.vo.collection.CollectionVO;
import org.notes.model.vo.collection.CreateCollectionVO;
import org.notes.scope.RequestScopeData;
import org.notes.service.CollectionService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final RequestScopeData requestScopeData;

    private final CollectionMapper collectionMapper;

    private final CollectionNoteMapper collectionNoteMapper;

    private final NoteMapper noteMapper;

    @Override
    public List<CollectionVO> getCollections(CollectionQueryParams queryParams) {
        Integer noteId = queryParams.getNoteId();

        List<Collection> collections = collectionMapper.findByCreatorId(queryParams.getCreatorId());
        List<Integer> collectionIds = collections.stream().map(Collection::getCollectionId).toList();
        // 收藏了特定笔记的收藏夹id
        final Set<Integer> collectedNoteIdCollectionIds;

        if (noteId != null) {
            collectedNoteIdCollectionIds = collectionNoteMapper.filterCollectionIdsByNoteId(noteId, collectionIds);
        } else {
            collectedNoteIdCollectionIds = Collections.emptySet();
        }

        List<CollectionVO> collectionVOS = collections.stream().map(collection -> {
            CollectionVO collectionVO = new CollectionVO();
            BeanUtils.copyProperties(collection, collectionVO);

            if (noteId == null) {
                return collectionVO;
            }

            CollectionVO.NoteStatus noteStatus = new CollectionVO.NoteStatus();
            noteStatus.setNoteId(noteId);
            noteStatus.setIsCollected(collectedNoteIdCollectionIds.contains(collection.getCollectionId()));
            collectionVO.setNoteStatus(noteStatus);

            return collectionVO;
        }).toList();

        return collectionVOS;
    }

    @Override
    @NeedLogin
    @Transactional(rollbackFor = Exception.class)
    public CreateCollectionVO createCollection(CreateCollectionBody requestBody) {
        Long creatorId = requestScopeData.getUserId();

        Collection collection = new Collection();
        collection.setCreatorId(creatorId);
        BeanUtils.copyProperties(requestBody, collection);

        try {
            collectionMapper.insert(collection);

            CreateCollectionVO createCollectionVO = new CreateCollectionVO();
            createCollectionVO.setCollectionId(collection.getCollectionId());

            return createCollectionVO;
        } catch (Exception e) {
            throw new BaseException("新建收藏夹失败");
        }
    }

    @Override
    @NeedLogin
    @Transactional(rollbackFor = Exception.class)
    public void updateCollection(Integer collectionId, UpdateCollectionBody requestBody) {
        Long creatorId = requestScopeData.getUserId();
        Collection collection = collectionMapper.findByIdAndCreatorId(collectionId, creatorId);

        if (collection == null) {
            throw new ForbiddenException("收藏夹不存在或者没有权限修改");
        }

        try {
            String name = requestBody.getName();
            String description = requestBody.getDescription();
            collection.setName(name);
            collection.setDescription(description);

            collectionMapper.update(collection);
        } catch (Exception e) {
            throw new BaseException("修改收藏夹失败");
        }
    }

    @Override
    @NeedLogin
    @Transactional(rollbackFor = Exception.class)
    public void deleteCollection(Integer collectionId) {
        Long creatorId = requestScopeData.getUserId();
        Collection collection = collectionMapper.findByIdAndCreatorId(collectionId, creatorId);

        if (collection == null) {
            throw new ForbiddenException("收藏夹不存在或者没有权限删除");
        }

        try {
            collectionMapper.deleteById(collectionId);
            collectionNoteMapper.deleteByCollectionId(collectionId);
        } catch (Exception e) {
            throw new BaseException("删除收藏夹失败");
        }
    }

}

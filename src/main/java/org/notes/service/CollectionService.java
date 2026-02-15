package org.notes.service;

import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.collection.CollectionQueryParams;
import org.notes.model.dto.collection.CreateCollectionBody;
import org.notes.model.dto.collection.UpdateCollectionBody;
import org.notes.model.vo.collection.CollectionVO;
import org.notes.model.vo.collection.CreateCollectionVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface CollectionService {

    ApiResponse<List<CollectionVO>> getCollections(CollectionQueryParams queryParams);

    ApiResponse<CreateCollectionVO> createCollection(CreateCollectionBody requestBody);

    ApiResponse<EmptyVO> updateCollection(Integer collectionId, UpdateCollectionBody requestBody);

    ApiResponse<EmptyVO> deleteCollection(Integer collectionId);
}

package org.notes.service;

import org.notes.model.dto.collection.CollectionQueryParams;
import org.notes.model.dto.collection.CreateCollectionBody;
import org.notes.model.dto.collection.UpdateCollectionBody;
import org.notes.model.vo.collection.CollectionVO;
import org.notes.model.vo.collection.CreateCollectionVO;

import java.util.List;

public interface CollectionService {

    List<CollectionVO> getCollections(CollectionQueryParams queryParams);

    CreateCollectionVO createCollection(CreateCollectionBody requestBody);

    void updateCollection(Integer collectionId, UpdateCollectionBody requestBody);

    void deleteCollection(Integer collectionId);
}

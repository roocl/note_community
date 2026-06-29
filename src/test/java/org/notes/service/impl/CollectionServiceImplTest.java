package org.notes.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.notes.exception.ForbiddenException;
import org.notes.mapper.CollectionMapper;
import org.notes.mapper.CollectionNoteMapper;
import org.notes.mapper.NoteMapper;
import org.notes.model.dto.collection.CollectionQueryParams;
import org.notes.model.dto.collection.CreateCollectionBody;
import org.notes.model.entity.Collection;
import org.notes.model.vo.collection.CollectionVO;
import org.notes.scope.RequestScopeData;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionServiceImplTest {

    @Mock
    private RequestScopeData requestScopeData;
    @Mock
    private CollectionMapper collectionMapper;
    @Mock
    private CollectionNoteMapper collectionNoteMapper;
    @Mock
    private NoteMapper noteMapper;

    @InjectMocks
    private CollectionServiceImpl collectionService;

    private Collection collection;

    @BeforeEach
    void setUp() {
        collection = new Collection();
        collection.setCollectionId(1);
        collection.setName("Favorites");
        collection.setCreatorId(1L);
    }

    @Test
    void getCollections_marksWhetherNoteIsCollected() {
        CollectionQueryParams params = new CollectionQueryParams();
        params.setCreatorId(1L);
        params.setNoteId(7);
        when(collectionMapper.findByCreatorId(1L)).thenReturn(List.of(collection));
        when(collectionNoteMapper.filterCollectionIdsByNoteId(7, List.of(1))).thenReturn(Set.of(1));

        List<CollectionVO> result = collectionService.getCollections(params);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getNoteStatus().getIsCollected());
    }

    @Test
    void createCollection_usesCurrentUser() {
        CreateCollectionBody body = new CreateCollectionBody();
        body.setName("New collection");
        when(requestScopeData.getUserId()).thenReturn(1L);

        assertNotNull(collectionService.createCollection(body));

        verify(collectionMapper).insert(any(Collection.class));
    }

    @Test
    void deleteCollection_throwsWhenUserDoesNotOwnIt() {
        when(requestScopeData.getUserId()).thenReturn(1L);
        when(collectionMapper.findByIdAndCreatorId(9, 1L)).thenReturn(null);

        assertThrows(ForbiddenException.class, () -> collectionService.deleteCollection(9));
    }
}

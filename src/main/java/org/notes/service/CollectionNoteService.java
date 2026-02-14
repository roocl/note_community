package org.notes.service;

import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.collectionNote.UpdateCollectionNoteBatchBody;
import org.notes.model.dto.collectionNote.UpdateCollectionNoteBody;
import org.notes.model.vo.note.NoteVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional
public interface CollectionNoteService {

    ApiResponse<List<NoteVO>> getCollectNotes(Integer collectionId);

    ApiResponse<EmptyVO> createCollectionNote(Integer collectionId, UpdateCollectionNoteBody requestBody);

    ApiResponse<EmptyVO> deleteCollectionNote(Integer collectionId, UpdateCollectionNoteBody requestBody);

    ApiResponse<EmptyVO> batchModifyCollection(UpdateCollectionNoteBatchBody requestBody);

    Set<Integer> findUserCollectedNoteIds(Long userId, List<Integer> noteIds);
}

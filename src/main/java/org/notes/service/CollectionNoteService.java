package org.notes.service;

import org.notes.model.dto.collectionNote.UpdateCollectionNoteBatchBody;
import org.notes.model.dto.collectionNote.UpdateCollectionNoteBody;
import org.notes.model.vo.note.NoteVO;

import java.util.List;
import java.util.Set;

public interface CollectionNoteService {

    List<NoteVO> getCollectNotes(Integer collectionId);

    void createCollectionNote(Integer collectionId, UpdateCollectionNoteBody requestBody);

    void deleteCollectionNote(Integer collectionId, UpdateCollectionNoteBody requestBody);

    void batchModifyCollection(UpdateCollectionNoteBatchBody requestBody);

    Set<Integer> findUserCollectedNoteIds(Long userId, List<Integer> noteIds);
}

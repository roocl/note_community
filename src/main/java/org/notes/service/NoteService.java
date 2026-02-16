package org.notes.service;

import org.notes.model.base.PageResult;
import org.notes.model.dto.note.CreateNoteRequest;
import org.notes.model.dto.note.NoteQueryParams;
import org.notes.model.dto.note.UpdateNoteRequest;
import org.notes.model.vo.note.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface NoteService {
    PageResult<List<NoteVO>> getNotes(NoteQueryParams params);

    CreateNoteVO createNote(CreateNoteRequest request);

    void updateNote(Integer noteId, UpdateNoteRequest request);

    void deleteNote(Integer noteId);

    List<NoteRankListItem> submitNoteRank();

    List<NoteHeatMapItem> submitNoteHeatMap();

    Top3Count submitNoteTop3Count();

}

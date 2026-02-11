package org.notes.service;

import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.note.CreateNoteRequest;
import org.notes.model.dto.note.NoteQueryParams;
import org.notes.model.dto.note.UpdateNoteRequest;
import org.notes.model.vo.note.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface NoteService {
    ApiResponse<List<NoteVO>> getNotes(NoteQueryParams params);

    ApiResponse<CreateNoteVO> createNote(CreateNoteRequest request);

    ApiResponse<EmptyVO> updateNote(Integer noteId, UpdateNoteRequest request);

    ApiResponse<EmptyVO> deleteNote(Integer noteId);

    ApiResponse<List<NoteRankListItem>> submitNoteRank();

    ApiResponse<List<NoteHeatMapItem>> submitNoteHeatMap();

    ApiResponse<Top3Count> submitNoteTop3Count();

}

package org.notes.controller;

import lombok.extern.log4j.Log4j2;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.note.CreateNoteRequest;
import org.notes.model.dto.note.NoteQueryParams;
import org.notes.model.dto.note.UpdateNoteRequest;
import org.notes.model.vo.note.*;
import org.notes.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/api")
public class NoteController {
    @Autowired
    NoteService noteService;

    @GetMapping("/notes")
    public ApiResponse<List<NoteVO>> getNotes(
            @Valid NoteQueryParams params) {
        return noteService.getNotes(params);
    }

    @PostMapping("/notes")
    public ApiResponse<CreateNoteVO> createNote(
            @Valid @RequestBody CreateNoteRequest request) {
        return noteService.createNote(request);
    }

    @PatchMapping("/notes/{noteId}")
    public ApiResponse<EmptyVO> updateNote(
            @Min(value = 1, message = "noteId 必须为正整数") @PathVariable Integer noteId,
            @Valid @RequestBody UpdateNoteRequest request) {
        return noteService.updateNote(noteId, request);
    }

    @DeleteMapping("/notes/{noteId}")
    public ApiResponse<EmptyVO> deleteNote(
            @Min(value = 1, message = "noteId 必须为正整数")
            @PathVariable Integer noteId) {
        return noteService.deleteNote(noteId);
    }

    @GetMapping("/notes/ranklist")
    public ApiResponse<List<NoteRankListItem>> submitNoteRank() {
        return noteService.submitNoteRank();
    }

    @GetMapping("/notes/heatmap")
    public ApiResponse<List<NoteHeatMapItem>> submitNoteHeatMap() {
        return noteService.submitNoteHeatMap();
    }

    @GetMapping("/notes/top3count")
    public ApiResponse<Top3Count> submitNoteTop3Count() {
        return noteService.submitNoteTop3Count();
    }
}

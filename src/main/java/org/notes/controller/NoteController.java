package org.notes.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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

@Api(tags = "笔记管理")
@Log4j2
@RestController
@RequestMapping("/api")
public class NoteController {
    @Autowired
    NoteService noteService;

    @ApiOperation("获取笔记列表")
    @GetMapping("/notes")
    public ApiResponse<List<NoteVO>> getNotes(
            @Valid NoteQueryParams params) {
        return noteService.getNotes(params);
    }

    @ApiOperation("创建笔记")
    @PostMapping("/notes")
    public ApiResponse<CreateNoteVO> createNote(
            @Valid @RequestBody CreateNoteRequest request) {
        return noteService.createNote(request);
    }

    @ApiOperation("更新笔记")
    @PatchMapping("/notes/{noteId}")
    public ApiResponse<EmptyVO> updateNote(
            @ApiParam("笔记ID") @Min(value = 1, message = "noteId 必须为正整数") @PathVariable Integer noteId,
            @Valid @RequestBody UpdateNoteRequest request) {
        return noteService.updateNote(noteId, request);
    }

    @ApiOperation("删除笔记")
    @DeleteMapping("/notes/{noteId}")
    public ApiResponse<EmptyVO> deleteNote(
            @ApiParam("笔记ID") @Min(value = 1, message = "noteId 必须为正整数") @PathVariable Integer noteId) {
        return noteService.deleteNote(noteId);
    }

    @ApiOperation("获取笔记排行榜")
    @GetMapping("/notes/ranklist")
    public ApiResponse<List<NoteRankListItem>> submitNoteRank() {
        return noteService.submitNoteRank();
    }

    @ApiOperation("获取笔记热力图数据")
    @GetMapping("/notes/heatmap")
    public ApiResponse<List<NoteHeatMapItem>> submitNoteHeatMap() {
        return noteService.submitNoteHeatMap();
    }

    @ApiOperation("获取笔记Top3统计")
    @GetMapping("/notes/top3count")
    public ApiResponse<Top3Count> submitNoteTop3Count() {
        return noteService.submitNoteTop3Count();
    }
}

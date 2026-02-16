package org.notes.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.dto.collectionNote.UpdateCollectionNoteBatchBody;
import org.notes.model.dto.collectionNote.UpdateCollectionNoteBody;
import org.notes.model.vo.note.NoteVO;
import org.notes.service.CollectionNoteService;
import org.notes.utils.ApiResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "收藏夹笔记管理")
@RestController
@RequestMapping("/api")
public class CollectionNoteController {

    @Autowired
    private CollectionNoteService collectionNoteService;

    @ApiOperation("获取收藏夹笔记列表")
    @GetMapping("/collectionNotes/{collectionId}")
    public ApiResponse<List<NoteVO>> getCollectionNote(@PathVariable Integer collectionId) {
        return ApiResponseUtil.success("查询收藏夹笔记成功", collectionNoteService.getCollectNotes(collectionId));
    }

    @ApiOperation("新增收藏夹笔记")
    @PostMapping("/collectionNotes/{collectionId}")
    public ApiResponse<EmptyVO> createCollectionNote(
            @PathVariable Integer collectionId,
            @Valid @RequestBody UpdateCollectionNoteBody requestBody) {
        collectionNoteService.createCollectionNote(collectionId, requestBody);
        return ApiResponseUtil.success("添加收藏夹笔记成功");
    }

    @ApiOperation("删除收藏夹笔记")
    @DeleteMapping("/collectionNotes/{collectionId}")
    public ApiResponse<EmptyVO> deleteCollectionNote(
            @PathVariable Integer collectionId,
            @Valid @RequestBody UpdateCollectionNoteBody requestBody) {
        collectionNoteService.deleteCollectionNote(collectionId, requestBody);
        return ApiResponseUtil.success("删除收藏夹笔记成功");
    }

    @ApiOperation("批量新增/删除收藏夹笔记")
    @PostMapping("/collectionNotes/batch")
    public ApiResponse<EmptyVO> batchModifyCollection(
            @Valid @RequestBody UpdateCollectionNoteBatchBody requestBody) {
        collectionNoteService.batchModifyCollection(requestBody);
        return ApiResponseUtil.success("操作成功");
    }
}

package org.notes.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.service.ElasticsearchSyncService;
import org.notes.utils.ApiResponseUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Elasticsearch 数据同步")
@RestController
@RequestMapping("/api/admin/es")
@RequiredArgsConstructor
public class ElasticsearchSyncController {

    private final ElasticsearchSyncService elasticsearchSyncService;

    @ApiOperation("全量同步所有数据到 Elasticsearch")
    @PostMapping("/sync-all")
    public ApiResponse<EmptyVO> syncAll() {
        elasticsearchSyncService.syncAll();
        return ApiResponseUtil.success("全量同步完成");
    }

    @ApiOperation("全量同步笔记到 Elasticsearch")
    @PostMapping("/sync-notes")
    public ApiResponse<EmptyVO> syncNotes() {
        elasticsearchSyncService.syncAllNotes();
        return ApiResponseUtil.success("笔记同步完成");
    }

    @ApiOperation("全量同步用户到 Elasticsearch")
    @PostMapping("/sync-users")
    public ApiResponse<EmptyVO> syncUsers() {
        elasticsearchSyncService.syncAllUsers();
        return ApiResponseUtil.success("用户同步完成");
    }
}

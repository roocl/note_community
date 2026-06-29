package org.notes.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.notes.annotation.NeedAdmin;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.EmptyVO;
import org.notes.model.entity.EsSyncFailure;
import org.notes.service.EsSyncFailureService;
import org.notes.utils.ApiResponseUtil;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "Elasticsearch sync compensation")
@RestController
@RequestMapping("/api/admin/es-sync-failures")
@RequiredArgsConstructor
public class EsSyncFailureController {

    private final EsSyncFailureService esSyncFailureService;

    @ApiOperation("获取ES同步失败列表")
    @GetMapping
    @NeedAdmin
    public ApiResponse<List<EsSyncFailure>> listFailures() {
        return ApiResponseUtil.success("已获取ES同步失败列表", esSyncFailureService.listFailures());
    }

    @ApiOperation("获取ES同步失败详情")
    @GetMapping("/{id}")
    @NeedAdmin
    public ApiResponse<EsSyncFailure> getFailure(@PathVariable Long id) {
        return ApiResponseUtil.success("已获取ES同步失败", esSyncFailureService.getFailure(id));
    }

    @ApiOperation("重试ES同步失败")
    @PostMapping("/{id}/retry")
    @NeedAdmin
    public ApiResponse<EmptyVO> retryFailure(@PathVariable Long id) {
        esSyncFailureService.retryFailure(id);
        return ApiResponseUtil.success("已重试ES同步失败");
    }

    @ApiOperation("对账MySQL和ES数据")
    @PostMapping("/reconcile")
    @NeedAdmin
    public ApiResponse<Integer> reconcile() {
        int mismatchCount = esSyncFailureService.reconcileAll();
        return ApiResponseUtil.success("已完成ES数据对账", mismatchCount);
    }

    @ApiOperation("删除ES同步失败")
    @DeleteMapping("/{id}")
    @NeedAdmin
    public ApiResponse<EmptyVO> deleteFailure(@PathVariable Long id) {
        esSyncFailureService.deleteFailure(id);
        return ApiResponseUtil.success("已删除ES同步失败");
    }
}

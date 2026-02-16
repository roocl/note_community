package org.notes.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.notes.model.base.ApiResponse;
import org.notes.model.vo.search.NoteSearchVO;
import org.notes.model.vo.search.UserSearchVO;
import org.notes.service.SearchService;
import org.notes.utils.ApiResponseUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import java.util.List;

@Api(tags = "搜索")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @ApiOperation("搜索笔记")
    @GetMapping("/notes")
    public ApiResponse<List<NoteSearchVO>> searchNotes(
            @ApiParam("搜索关键词") @RequestParam String keyword,
            @ApiParam("页码") @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "20") @Min(1) Integer pageSize) {
        return ApiResponseUtil.success("搜索成功", searchService.searchNotes(keyword, page, pageSize));
    }

    @ApiOperation("搜索用户")
    @GetMapping("/users")
    public ApiResponse<List<UserSearchVO>> searchUsers(
            @ApiParam("搜索关键词") @RequestParam String keyword,
            @ApiParam("页码") @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "20") @Min(1) Integer pageSize) {
        return ApiResponseUtil.success("搜索成功", searchService.searchUsers(keyword, page, pageSize));
    }

}

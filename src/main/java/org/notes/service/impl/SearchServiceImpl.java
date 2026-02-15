package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.notes.model.base.ApiResponse;
import org.notes.model.es.NoteDocument;
import org.notes.model.es.UserDocument;
import org.notes.model.vo.search.NoteSearchVO;
import org.notes.model.vo.search.UserSearchVO;
import org.notes.service.SearchService;
import org.notes.utils.ApiResponseUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public ApiResponse<List<NoteSearchVO>> searchNotes(String keyword, int page, int pageSize) {
        try {
            // 构建高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field(new HighlightBuilder.Field("content")
                    .preTags("<em>")
                    .postTags("</em>")
                    .fragmentSize(200)
                    .numOfFragments(1));

            // 构建查询
            NativeSearchQuery query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.matchQuery("content", keyword))
                    .withHighlightBuilder(highlightBuilder)
                    .withPageable(PageRequest.of(page - 1, pageSize))
                    .build();

            SearchHits<NoteDocument> searchHits = elasticsearchOperations.search(query, NoteDocument.class);

            List<NoteSearchVO> results = searchHits.getSearchHits().stream().map(hit -> {
                NoteSearchVO vo = new NoteSearchVO();
                BeanUtils.copyProperties(hit.getContent(), vo);

                // 用高亮内容替换原始内容
                Map<String, List<String>> highlightFields = hit.getHighlightFields();
                if (highlightFields.containsKey("content") && !highlightFields.get("content").isEmpty()) {
                    vo.setContent(highlightFields.get("content").get(0));
                }

                return vo;
            }).toList();

            return ApiResponseUtil.success("搜索成功", results);
        } catch (Exception e) {
            log.error("搜索笔记失败", e);
            return ApiResponseUtil.error("搜索失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<UserSearchVO>> searchUsers(String keyword, int page, int pageSize) {
        try {
            // 构建高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field(new HighlightBuilder.Field("username")
                    .preTags("<em>")
                    .postTags("</em>"));
            highlightBuilder.field(new HighlightBuilder.Field("school")
                    .preTags("<em>")
                    .postTags("</em>"));
            highlightBuilder.field(new HighlightBuilder.Field("signature")
                    .preTags("<em>")
                    .postTags("</em>"));

            // 构建查询：multi_match 搜索 username、account、email、school、signature
            NativeSearchQuery query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.multiMatchQuery(keyword,
                            "username", "account", "email", "school", "signature"))
                    .withHighlightBuilder(highlightBuilder)
                    .withPageable(PageRequest.of(page - 1, pageSize))
                    .build();

            SearchHits<UserDocument> searchHits = elasticsearchOperations.search(query, UserDocument.class);

            List<UserSearchVO> results = searchHits.getSearchHits().stream().map(hit -> {
                UserSearchVO vo = new UserSearchVO();
                BeanUtils.copyProperties(hit.getContent(), vo);

                // 用高亮内容替换原始字段
                Map<String, List<String>> highlightFields = hit.getHighlightFields();
                if (highlightFields.containsKey("username") && !highlightFields.get("username").isEmpty()) {
                    vo.setUsername(highlightFields.get("username").get(0));
                }
                if (highlightFields.containsKey("school") && !highlightFields.get("school").isEmpty()) {
                    vo.setSchool(highlightFields.get("school").get(0));
                }
                if (highlightFields.containsKey("signature") && !highlightFields.get("signature").isEmpty()) {
                    vo.setSignature(highlightFields.get("signature").get(0));
                }

                return vo;
            }).toList();

            return ApiResponseUtil.success("搜索成功", results);
        } catch (Exception e) {
            log.error("搜索用户失败", e);
            return ApiResponseUtil.error("搜索失败: " + e.getMessage());
        }
    }
}

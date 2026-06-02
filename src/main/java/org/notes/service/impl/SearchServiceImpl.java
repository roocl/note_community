package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.springframework.data.elasticsearch.core.AggregationsContainer;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest;
import org.notes.mapper.NoteMapper;
import org.notes.mapper.UserMapper;
import org.notes.model.entity.Note;
import org.notes.model.entity.User;
import org.notes.model.es.NoteDocument;
import org.notes.model.es.UserDocument;
import org.notes.model.vo.search.NoteSearchResultVO;
import org.notes.model.vo.search.NoteSearchVO;
import org.notes.model.vo.search.UserSearchVO;
import org.notes.service.SearchService;
import org.notes.utils.PaginationUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    private final NoteMapper noteMapper;

    private final UserMapper userMapper;

    @Override
    public NoteSearchResultVO searchNotes(String keyword, int page, int pageSize) {
        try {
            return searchNotesViaEsWithAggs(keyword, page, pageSize);
        } catch (Exception e) {
            log.warn("ES 搜索笔记失败，降级为 MySQL 查询", e);
            List<NoteSearchVO> notes = searchNotesViaMysql(keyword, page, pageSize);
            NoteSearchResultVO result = new NoteSearchResultVO();
            result.setNotes(notes);
            result.setCategoryAggs(Collections.emptyList());
            return result;
        }
    }

    private NoteSearchResultVO searchNotesViaEsWithAggs(String keyword, int page, int pageSize) {
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(new HighlightBuilder.Field("title")
                .preTags("<em>")
                .postTags("</em>")
                .fragmentSize(100)
                .numOfFragments(1));
        highlightBuilder.field(new HighlightBuilder.Field("content")
                .preTags("<em>")
                .postTags("</em>")
                .fragmentSize(200)
                .numOfFragments(1));

        // 分类聚合
        TermsAggregationBuilder categoryAgg = AggregationBuilders.terms("categories")
                .field("categoryName")
                .size(20);

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword)
                        .field("title", 3.0f)
                        .field("content", 1.0f))
                .withHighlightBuilder(highlightBuilder)
                .withAggregations(categoryAgg)
                .withPageable(PageRequest.of(page - 1, pageSize))
                .build();

        SearchHits<NoteDocument> searchHits = elasticsearchOperations.search(query, NoteDocument.class);

        List<NoteSearchVO> notes = searchHits.getSearchHits().stream().map(hit -> {
            NoteSearchVO vo = new NoteSearchVO();
            BeanUtils.copyProperties(hit.getContent(), vo);

            Map<String, List<String>> highlightFields = hit.getHighlightFields();
            if (highlightFields.containsKey("title") && !highlightFields.get("title").isEmpty()) {
                vo.setTitle(highlightFields.get("title").get(0));
            }
            if (highlightFields.containsKey("content") && !highlightFields.get("content").isEmpty()) {
                vo.setContent(highlightFields.get("content").get(0));
            }

            return vo;
        }).toList();

        // 提取聚合结果
        List<NoteSearchResultVO.CategoryAgg> aggs = new ArrayList<>();
        AggregationsContainer<?> container = searchHits.getAggregations();
        if (container != null) {
            Object rawAggs = container.aggregations();
            if (rawAggs instanceof org.elasticsearch.search.aggregations.Aggregations esAggs) {
                Terms terms = esAggs.get("categories");
                if (terms != null) {
                    for (Terms.Bucket bucket : terms.getBuckets()) {
                        NoteSearchResultVO.CategoryAgg agg = new NoteSearchResultVO.CategoryAgg();
                        agg.setName(bucket.getKeyAsString());
                        agg.setCount(bucket.getDocCount());
                        aggs.add(agg);
                    }
                }
            }
        }

        NoteSearchResultVO result = new NoteSearchResultVO();
        result.setNotes(notes);
        result.setCategoryAggs(aggs);
        return result;
    }

    private List<NoteSearchVO> searchNotesViaMysql(String keyword, int page, int pageSize) {
        int offset = PaginationUtils.calculateOffset(page, pageSize);
        List<Note> notes = noteMapper.searchNotes(keyword, pageSize, offset);
        if (notes == null) {
            return Collections.emptyList();
        }
        return notes.stream().map(note -> {
            NoteSearchVO vo = new NoteSearchVO();
            BeanUtils.copyProperties(note, vo);
            return vo;
        }).toList();
    }

    @Override
    public List<UserSearchVO> searchUsers(String keyword, int page, int pageSize) {
        try {
            return searchUsersViaEs(keyword, page, pageSize);
        } catch (Exception e) {
            log.warn("ES 搜索用户失败，降级为 MySQL 查询", e);
            return searchUsersViaMysql(keyword, page, pageSize);
        }
    }

    private List<UserSearchVO> searchUsersViaEs(String keyword, int page, int pageSize) {
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

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword,
                        "username", "account", "email", "school", "signature"))
                .withHighlightBuilder(highlightBuilder)
                .withPageable(PageRequest.of(page - 1, pageSize))
                .build();

        SearchHits<UserDocument> searchHits = elasticsearchOperations.search(query, UserDocument.class);

        return searchHits.getSearchHits().stream().map(hit -> {
            UserSearchVO vo = new UserSearchVO();
            BeanUtils.copyProperties(hit.getContent(), vo);

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
    }

    @Override
    public List<String> suggest(String keyword) {
        try {
            NativeSearchQuery query = new NativeSearchQueryBuilder()
                    .withSuggestBuilder(new SuggestBuilder()
                            .addSuggestion("note-suggest",
                                    SuggestBuilders.completionSuggestion("suggest")
                                            .prefix(keyword)
                                            .size(5)
                                            .skipDuplicates(true)))
                    .build();

            SearchHits<NoteDocument> searchHits = elasticsearchOperations.search(query, NoteDocument.class);
            Suggest suggest = searchHits.getSuggest();

            if (suggest == null) {
                return Collections.emptyList();
            }

            var completionSuggestion = suggest.getSuggestion("note-suggest");

            if (completionSuggestion == null) {
                return Collections.emptyList();
            }

            return completionSuggestion.getEntries().stream()
                    .flatMap(entry -> entry.getOptions().stream())
                    .map(option -> option.getText())
                    .distinct()
                    .toList();
        } catch (Exception e) {
            log.warn("ES 搜索建议失败", e);
            return Collections.emptyList();
        }
    }

    private List<UserSearchVO> searchUsersViaMysql(String keyword, int page, int pageSize) {
        int offset = PaginationUtils.calculateOffset(page, pageSize);
        List<User> users = userMapper.searchUsers(keyword, pageSize, offset);
        if (users == null) {
            return Collections.emptyList();
        }
        return users.stream().map(user -> {
            UserSearchVO vo = new UserSearchVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        }).toList();
    }
}

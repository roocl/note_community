package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.notes.mapper.NoteMapper;
import org.notes.mapper.UserMapper;
import org.notes.model.entity.Note;
import org.notes.model.entity.User;
import org.notes.model.es.NoteDocument;
import org.notes.model.es.UserDocument;
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
    public List<NoteSearchVO> searchNotes(String keyword, int page, int pageSize) {
        try {
            return searchNotesViaEs(keyword, page, pageSize);
        } catch (Exception e) {
            log.warn("ES 搜索笔记失败，降级为 MySQL 查询", e);
            return searchNotesViaMysql(keyword, page, pageSize);
        }
    }

    private List<NoteSearchVO> searchNotesViaEs(String keyword, int page, int pageSize) {
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(new HighlightBuilder.Field("content")
                .preTags("<em>")
                .postTags("</em>")
                .fragmentSize(200)
                .numOfFragments(1));

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("content", keyword))
                .withHighlightBuilder(highlightBuilder)
                .withPageable(PageRequest.of(page - 1, pageSize))
                .build();

        SearchHits<NoteDocument> searchHits = elasticsearchOperations.search(query, NoteDocument.class);

        return searchHits.getSearchHits().stream().map(hit -> {
            NoteSearchVO vo = new NoteSearchVO();
            BeanUtils.copyProperties(hit.getContent(), vo);

            Map<String, List<String>> highlightFields = hit.getHighlightFields();
            if (highlightFields.containsKey("content") && !highlightFields.get("content").isEmpty()) {
                vo.setContent(highlightFields.get("content").get(0));
            }

            return vo;
        }).toList();
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

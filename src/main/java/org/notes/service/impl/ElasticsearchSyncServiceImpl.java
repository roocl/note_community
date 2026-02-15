package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.notes.mapper.NoteMapper;
import org.notes.mapper.UserMapper;
import org.notes.model.entity.Note;
import org.notes.model.entity.User;
import org.notes.model.es.NoteDocument;
import org.notes.model.es.UserDocument;
import org.notes.repository.NoteSearchRepository;
import org.notes.repository.UserSearchRepository;
import org.notes.service.ElasticsearchSyncService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Elasticsearch 全量同步服务
 * 用于将 MySQL 存量数据批量导入 ES
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class ElasticsearchSyncServiceImpl implements ElasticsearchSyncService {

    private final NoteMapper noteMapper;

    private final UserMapper userMapper;

    private final NoteSearchRepository noteSearchRepository;

    private final UserSearchRepository userSearchRepository;

    /**
     * 全量同步所有笔记到 Elasticsearch
     */
    public void syncAllNotes() {
        log.info("开始全量同步笔记到 Elasticsearch");
        try {
            // 查询所有笔记
            List<Note> notes = noteMapper.findAll();
            List<NoteDocument> docs = notes.stream().map(note -> {
                NoteDocument doc = new NoteDocument();
                BeanUtils.copyProperties(note, doc);
                return doc;
            }).toList();

            noteSearchRepository.saveAll(docs);
            log.info("笔记全量同步完成，共同步 {} 条数据", docs.size());
        } catch (Exception e) {
            log.error("笔记全量同步失败", e);
            throw new RuntimeException("笔记全量同步失败", e);
        }
    }

    /**
     * 全量同步所有用户到 Elasticsearch
     */
    public void syncAllUsers() {
        log.info("开始全量同步用户到 Elasticsearch...");
        try {
            // 查询所有用户
            List<User> users = userMapper.findAll();
            List<UserDocument> docs = users.stream().map(user -> {
                UserDocument doc = new UserDocument();
                BeanUtils.copyProperties(user, doc);
                return doc;
            }).toList();

            userSearchRepository.saveAll(docs);
            log.info("用户全量同步完成，共同步 {} 条数据", docs.size());
        } catch (Exception e) {
            log.error("用户全量同步失败", e);
            throw new RuntimeException("用户全量同步失败", e);
        }
    }

    /**
     * 全量同步所有数据到 Elasticsearch
     */
    public void syncAll() {
        syncAllNotes();
        syncAllUsers();
    }
}

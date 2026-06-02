package org.notes.task.es;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.notes.mapper.NoteMapper;
import org.notes.model.entity.Category;
import org.notes.model.entity.Note;
import org.notes.model.entity.Question;
import org.notes.model.es.NoteDocument;
import org.notes.repository.NoteSearchRepository;
import org.notes.service.CategoryService;
import org.notes.service.QuestionService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ES 定时对账任务
 * 每30分钟对比 MySQL 与 ES 中的数据，自动修复差异
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EsReconciliationTask {

    private final NoteMapper noteMapper;

    private final NoteSearchRepository noteSearchRepository;

    private final QuestionService questionService;

    private final CategoryService categoryService;

    /**
     * 每30分钟执行一次 ES 与 MySQL 数据对账
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void reconcile() {
        log.info("[ES对账] 开始执行...");

        try {
            // 1. 获取 MySQL 所有笔记 ID
            List<Note> allNotes = noteMapper.findAll();
            Set<Integer> mysqlIds = allNotes.stream()
                    .map(Note::getNoteId)
                    .collect(Collectors.toSet());

            // 2. 分页遍历 ES，收集所有笔记 ID
            List<Integer> esIds = new ArrayList<>();
            long esTotal = noteSearchRepository.count();
            int batchSize = 1000;
            int pages = (int) Math.ceil((double) esTotal / batchSize);
            for (int i = 0; i < pages && i < 50; i++) {
                Page<NoteDocument> page = noteSearchRepository.findAll(PageRequest.of(i, batchSize));
                for (NoteDocument doc : page.getContent()) {
                    esIds.add(doc.getNoteId());
                }
            }

            Set<Integer> esIdSet = new HashSet<>(esIds);

            // 3. MySQL 有、ES 无 → 补录
            List<Integer> missingInEs = mysqlIds.stream()
                    .filter(id -> !esIdSet.contains(id))
                    .toList();

            // 4. ES 有、MySQL 无（幽灵数据）→ 删除
            List<Integer> ghostInEs = esIds.stream()
                    .filter(id -> !mysqlIds.contains(id))
                    .toList();

            if (!missingInEs.isEmpty()) {
                log.warn("[ES对账] ES 缺失 {} 条，正在修复...", missingInEs.size());
                List<Note> notesToFix = allNotes.stream()
                        .filter(n -> missingInEs.contains(n.getNoteId()))
                        .toList();

                List<NoteDocument> docsToAdd = new ArrayList<>();
                for (Note note : notesToFix) {
                    NoteDocument doc = new NoteDocument();
                    BeanUtils.copyProperties(note, doc);

                    // 从关联 Question 获取标题和分类名
                    Integer qId = note.getQuestionId();
                    if (qId != null) {
                        Question question = questionService.findById(qId);
                        if (question != null) {
                            doc.setTitle(question.getTitle());
                            doc.setSuggest(new Completion(new String[]{question.getTitle()}));
                            if (question.getCategoryId() != null) {
                                Category cat = categoryService.findById(question.getCategoryId());
                                if (cat != null) {
                                    doc.setCategoryName(cat.getName());
                                }
                            }
                        }
                    }
                    docsToAdd.add(doc);
                }
                noteSearchRepository.saveAll(docsToAdd);
                log.info("[ES对账] 已修复 {} 条", docsToAdd.size());
            }

            if (!ghostInEs.isEmpty()) {
                log.warn("[ES对账] ES 幽灵数据 {} 条，正在删除...", ghostInEs.size());
                noteSearchRepository.deleteAllById(ghostInEs);
                log.info("[ES对账] 已删除 {} 条", ghostInEs.size());
            }

            if (missingInEs.isEmpty() && ghostInEs.isEmpty()) {
                log.info("[ES对账] 数据一致。MySQL={}, ES={}", mysqlIds.size(), esIds.size());
            }
        } catch (Exception e) {
            log.error("[ES对账] 执行失败", e);
        }
    }
}

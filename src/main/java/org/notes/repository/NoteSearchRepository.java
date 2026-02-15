package org.notes.repository;

import org.notes.model.es.NoteDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 笔记 Elasticsearch Repository
 */
public interface NoteSearchRepository extends ElasticsearchRepository<NoteDocument, Integer> {
}

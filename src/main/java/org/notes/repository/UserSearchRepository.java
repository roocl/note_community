package org.notes.repository;

import org.notes.model.es.UserDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 用户 Elasticsearch Repository
 */
public interface UserSearchRepository extends ElasticsearchRepository<UserDocument, Long> {
}

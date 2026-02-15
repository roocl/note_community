package org.notes.model.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * Elasticsearch 笔记文档
 */
@Data
@Document(indexName = "notes")
public class NoteDocument {

    @Id
    private Integer noteId;

    @Field(type = FieldType.Long)
    private Long authorId;

    @Field(type = FieldType.Integer)
    private Integer questionId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Integer)
    private Integer likeCount;

    @Field(type = FieldType.Integer)
    private Integer commentCount;

    @Field(type = FieldType.Integer)
    private Integer collectCount;

    @Field(type = FieldType.Date,
            format = DateFormat.custom,
            pattern = "uuuu-MM-dd HH:mm:ss || uuuu-MM-dd")
    private LocalDateTime createdAt;
}

package org.notes.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.notes.model.dto.question.QuestionQueryParams;
import org.notes.model.entity.Question;

import java.util.List;

/**
 * 题目表(Question)表数据库访问层
 *
 * @author makejava
 * @since 2026-02-06 09:19:38
 */
@Mapper
public interface QuestionMapper {

    Question findById(@Param("questionId") Integer questionId);

    List<Question> findByIdBatch(@Param("questionIds") List<Integer> questionIds);

    List<Question> findByQueryParam(@Param("queryParam") QuestionQueryParams queryParam,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);

    Question findByTitle(@Param("title") String title);

    List<Question> findByKeyword(@Param("keyword") String keyword);

    int insert(Question question);

    int update(@Param("question") Question question);

    int deleteById(Integer questionId);

    int deleteByCategoryId(Integer categoryId);

    int deleteByCategoryIdBatch(@Param("categoryIds") List<Integer> categoryIds);

    int incrementViewCount(@Param("questionId") Integer questionId);

    int countByQueryParam(@Param("queryParam") QuestionQueryParams queryParam);

}


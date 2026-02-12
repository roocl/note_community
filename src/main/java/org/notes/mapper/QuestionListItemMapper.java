package org.notes.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.notes.model.entity.QuestionListItem;
import org.notes.model.vo.questionListItem.QuestionListItemVO;

import java.util.List;

@Mapper
public interface QuestionListItemMapper {

    int insert(QuestionListItem questionListItem);

    List<QuestionListItemVO> findByQuestionListId(@Param("questionListId") Integer questionListId);

    int countByQuestionListId(@Param("questionListId") Integer questionListId);

    List<QuestionListItemVO> findByQuestionListIdPage(@Param("questionListId") Integer questionListId,
                                                      @Param("limit") Integer limit,
                                                      @Param("offset") Integer offset);

    int deleteByQuestionListId(Integer questionListId);

    int deleteByQuestionListIdAndQuestionId(@Param("questionListId") Integer questionListId, @Param("questionId") Integer questionId);

    int nextRank(Integer questionListId);

    int updateQuestionRank(QuestionListItem questionListItem);
}
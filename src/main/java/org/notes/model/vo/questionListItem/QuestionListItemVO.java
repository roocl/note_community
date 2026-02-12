package org.notes.model.vo.questionListItem;

import lombok.Data;
import org.notes.model.vo.question.BaseQuestionVO;

@Data
public class QuestionListItemVO {
    /*
     * 题单ID（联合主键）
     */
    private Integer questionListId;

    /*
     * 题目ID（联合主键）
     */
    private BaseQuestionVO question;

    /*
     * 题单内题目的顺序，从1开始
     */
    private Integer rank;
}
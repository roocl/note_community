package org.notes.model.vo.question;

import lombok.Data;

import java.util.Date;

@Data
public class QuestionNoteVO {
    private String title;
    /**
     * 问题难度: 1=简单, 2=中等, 3=困难
     */
    private Integer difficulty;
    /**
     * 题目考点
     */
    private String examPoint;
    /**
     * 浏览量
     */
    private Integer viewCount;

    private UserNote userNote;

    @Data
    public static class UserNote {
        /*
         * 是否完成
         */
        private boolean finished = false;
        /**
         * noteId
         */
        private Integer noteId;
        /**
         * 笔记内容
         */
        private String content;
    }
}

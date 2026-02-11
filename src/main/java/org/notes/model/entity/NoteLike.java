package org.notes.model.entity;

import lombok.Data;

import java.util.Date;

@Data
public class NoteLike {
    /*
     * 笔记ID（联合主键）
     */
    private Integer noteId;

    /*
     * 点赞用户ID（联合主键）
     */
    private Long userId;

    /*
     * 创建时间
     */
    private Date createdAt;

    /*
     * 更新时间
     */
    private Date updatedAt;
}
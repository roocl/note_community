package org.notes.model.entity;

import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * 题目表(Question)实体类
 *
 * @author makejava
 * @since 2026-02-06 09:19:40
 */
@Data
public class Question implements Serializable {
    /**
     * 问题 ID
     */
    private Integer questionId;
    /**
     * 问题所属分类 ID
     */
    private Integer categoryId;
    /**
     * 问题标题
     */
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
    /**
     * 记录创建时间
     */
    private Date createdAt;
    /**
     * 记录更新时间
     */
    private Date updatedAt;
}


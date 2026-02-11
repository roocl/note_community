package org.notes.model.dto.comment;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateCommentRequest {
    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;
}

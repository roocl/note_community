package org.notes.model.dto.note;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateNoteRequest {
    /*
     * 问题ID
     */
    @NotNull(message = "问题 ID 不能为空")
    @Min(value = 1, message = "问题 ID 必须为正整数")
    private Integer questionId;

    /*
     * 笔记内容
     */
    @NotBlank(message = "笔记内容不能为空")
    @NotNull(message = "笔记内容不能为空")
    private String content;
}

package org.notes.model.dto.note;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UpdateNoteRequest {
    @NotNull(message = "笔记内容不能为空")
    @NotBlank(message = "笔记内容不能为空")
    private String content;
}

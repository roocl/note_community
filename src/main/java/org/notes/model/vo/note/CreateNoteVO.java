package org.notes.model.vo.note;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("创建笔记结果")
@Data
public class CreateNoteVO {
    @ApiModelProperty("新建笔记ID")
    private Integer noteId;
}

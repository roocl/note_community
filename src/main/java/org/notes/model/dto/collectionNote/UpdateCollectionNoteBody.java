package org.notes.model.dto.collectionNote;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;

@ApiModel("新增收藏夹笔记请求")
@Data
public class UpdateCollectionNoteBody {
    @ApiModelProperty("笔记ID")
    @Min(value = 1, message = "noteId 必须为正整数")
    private Integer noteId;
}

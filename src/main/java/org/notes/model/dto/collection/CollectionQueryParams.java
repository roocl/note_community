package org.notes.model.dto.collection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ApiModel("收藏夹查询参数")
@Data
public class CollectionQueryParams {
    @ApiModelProperty(value = "创建者ID", required = true)
    @NotNull(message = "creatorId 不能为空")
    @Min(value = 1, message = "creatorId 必须为正整数")
    private Long creatorId;

    @ApiModelProperty("笔记ID")
    @Min(value = 1, message = "noteId 必须为正整数")
    private Integer noteId;
}

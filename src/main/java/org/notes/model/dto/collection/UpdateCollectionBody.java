package org.notes.model.dto.collection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@ApiModel("批量修改收藏夹请求")
@Data
public class UpdateCollectionBody {
    @ApiModelProperty("笔记ID")
    @Min(value = 1, message = "noteId 必须为正整数")
    private Integer noteId;

    @ApiModelProperty("收藏夹操作列表")
    private UpdateItem[] collections;

    @ApiModel("收藏夹操作项")
    @Data
    public static class UpdateItem {
        @ApiModelProperty("收藏夹ID")
        @Min(value = 1, message = "collectionId 必须为正整数")
        private Integer collectionId;

        @ApiModelProperty(value = "操作类型: create 或 delete", required = true)
        @NotNull(message = "action 不能为空")
        @NotEmpty(message = "action 不能为空")
        @Pattern(regexp = "create|delete", message = "action 必须为 create 或者 delete")
        private String action;
    }
}

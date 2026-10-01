package org.notes.model.dto.collectionNote;

import io.swagger.annotations.ApiModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@ApiModel("批量修改收藏夹笔记请求")
@Data
public class UpdateCollectionNoteBatchBody {
    @ApiModelProperty("笔记ID")
    @NotNull(message = "noteId 不能为空")
    @Min(value = 1, message = "noteId 必须为正整数")
    private Integer noteId;

    @ApiModelProperty("收藏夹操作列表")
    @NotNull(message = "collections 不能为空")
    @Valid
    private UpdateItem[] collections;

    @ApiModel("收藏夹操作项")
    @Data
    public static class UpdateItem {
        @ApiModelProperty("收藏夹ID")
        @Min(value = 1, message = "collectionId 必须为正整数")
        @NotNull(message = "collectionId 不能为空")
        private Integer collectionId;

        @ApiModelProperty(value = "操作类型: create 或 delete", required = true)
        @NotNull(message = "action 不能为空")
        private Action action;
    }

    public enum Action {
        @JsonProperty("create")
        CREATE,
        @JsonProperty("delete")
        DELETE
    }
}

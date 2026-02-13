package org.notes.model.vo.collection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("收藏夹VO")
@Data
public class CollectionVO {
    @ApiModelProperty("收藏夹ID")
    private Integer collectionId;

    @ApiModelProperty("收藏夹名称")
    private String name;

    @ApiModelProperty("收藏夹描述")
    private String description;

    @ApiModelProperty("笔记收藏状态")
    private NoteStatus noteStatus;

    @ApiModel("笔记收藏状态")
    @Data
    public static class NoteStatus {
        @ApiModelProperty("笔记ID")
        private Integer noteId;

        @ApiModelProperty("是否已收藏")
        private Boolean isCollected;
    }
}
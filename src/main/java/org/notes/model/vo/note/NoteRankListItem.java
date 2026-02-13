package org.notes.model.vo.note;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("笔记排行榜项")
@Data
public class NoteRankListItem {
    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("头像URL")
    private String avatarUrl;

    @ApiModelProperty("笔记数量")
    private Integer noteCount;

    @ApiModelProperty("排名")
    private Integer rank;
}

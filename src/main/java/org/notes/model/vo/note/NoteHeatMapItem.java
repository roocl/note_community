package org.notes.model.vo.note;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

@ApiModel("笔记热力图项")
@Data
public class NoteHeatMapItem {
    @ApiModelProperty("日期")
    private LocalDate date;

    @ApiModelProperty("笔记数量")
    private Integer count;

    @ApiModelProperty("排名")
    private Integer rank;
}

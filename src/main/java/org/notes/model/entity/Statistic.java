package org.notes.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

@ApiModel("统计数据")
@Data
public class Statistic {
    @ApiModelProperty("主键ID")
    private Integer id;

    @ApiModelProperty("当天登录次数")
    private Integer loginCount;

    @ApiModelProperty("当天注册人数")
    private Integer registerCount;

    @ApiModelProperty("累计注册总人数")
    private Integer totalRegisterCount;

    @ApiModelProperty("当天笔记数量")
    private Integer noteCount;

    @ApiModelProperty("当天提交的笔记数量")
    private Integer submitNoteCount;

    @ApiModelProperty("累计笔记总数量")
    private Integer totalNoteCount;

    @ApiModelProperty("统计日期")
    private LocalDate date;
}
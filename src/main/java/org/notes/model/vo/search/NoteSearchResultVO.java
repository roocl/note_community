package org.notes.model.vo.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("笔记搜索结果（含分类聚合）")
@Data
public class NoteSearchResultVO {

    @ApiModelProperty("搜索结果列表")
    private List<NoteSearchVO> notes;

    @ApiModelProperty("分类聚合统计")
    private List<CategoryAgg> categoryAggs;

    @ApiModel("分类聚合项")
    @Data
    public static class CategoryAgg {
        @ApiModelProperty("分类名称")
        private String name;

        @ApiModelProperty("该分类下的笔记数量")
        private long count;
    }
}

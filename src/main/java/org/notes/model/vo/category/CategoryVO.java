package org.notes.model.vo.category;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("分类VO")
@Data
public class CategoryVO {
    @ApiModelProperty("分类ID")
    private Integer categoryId;

    @ApiModelProperty("分类名称")
    private String name;

    @ApiModelProperty("上级分类ID")
    private Integer parentCategoryId;

    @ApiModelProperty("子分类列表")
    private List<ChildrenCategoryVO> children;

    @ApiModel("子分类VO")
    @Data
    public static class ChildrenCategoryVO {
        @ApiModelProperty("分类ID")
        private Integer categoryId;

        @ApiModelProperty("分类名称")
        private String name;

        @ApiModelProperty("上级分类ID")
        private Integer parentCategoryId;
    }
}
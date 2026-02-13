package org.notes.model.dto.note;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Arrays;
import java.util.List;

@ApiModel("笔记查询参数")
@Data
public class NoteQueryParams {

        @ApiModelProperty("题目ID")
        private Integer questionId;

        @ApiModelProperty("作者ID")
        private Long authorId;

        @ApiModelProperty("收藏夹ID")
        private Integer collectionId;

        @ApiModelProperty("排序字段")
        private String sortBy;

        @ApiModelProperty("排序方向: asc 或 desc")
        private String sortOrder;

        @ApiModelProperty(value = "页码", example = "1")
        @Min(value = 1, message = "page 必须为正整数")
        private Integer page = 1;

        @ApiModelProperty(value = "每页大小", example = "10")
        @Min(value = 1, message = "pageSize 必须为正整数")
        @Max(value = 100, message = "pageSize 最大为 100")
        private Integer pageSize = 10;

        @ApiModelProperty("最近N天的笔记")
        @Min(value = 1, message = "recentDays 必须为正整数")
        @Max(value = 365, message = "recentDays 最大为 365")
        private Integer recentDays;

        @AssertTrue(message = "sortBy 不合法，仅允许: created_at, updated_at, like_count, comment_count, collect_count")
        private boolean isValidSortBy() {
                if (sortBy == null)
                        return true;
                List<String> allowedSortBy = Arrays.asList("created_at", "updated_at", "like_count", "comment_count",
                                "collect_count");
                return allowedSortBy.contains(sortBy);
        }

        @AssertTrue(message = "sortOrder 不合法，仅允许: asc, desc")
        private boolean isValidSortOrder() {
                if (sortOrder == null)
                        return true;
                List<String> allowedSortOrder = Arrays.asList("asc", "desc");
                return allowedSortOrder.contains(sortOrder);
        }
}

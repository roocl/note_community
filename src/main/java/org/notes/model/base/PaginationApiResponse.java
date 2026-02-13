package org.notes.model.base;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("分页API响应")
public class PaginationApiResponse<T> extends ApiResponse<T> {
    @ApiModelProperty("分页信息")
    private final Pagination pagination;

    public PaginationApiResponse(int code, String message, T data, Pagination pagination) {
        super(code, message, data);
        this.pagination = pagination;
    }

    public Pagination getPagination() {
        return this.pagination;
    }
}

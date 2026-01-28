package org.notes.model.base;

public class PaginationApiResponse<T> extends ApiResponse<T> {
    private final Pagination pagination;

    public PaginationApiResponse(int code, String message, T data, Pagination pagination) {
        super(code, message, data);
        this.pagination = pagination;
    }

    public Pagination getPagination() {
        return this.pagination;
    }
}

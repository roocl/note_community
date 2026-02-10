package org.notes.utils;

import org.notes.model.base.ApiResponse;
import org.notes.model.base.Pagination;
import org.notes.model.base.PaginationApiResponse;
import org.notes.model.base.TokenApiResponse;
import org.springframework.http.HttpStatus;

public class ApiResponseUtil {

    public static <T> ApiResponse<T> success(String msg, T data) {
        return ApiResponse.success(msg, data);
    }

    public static <T> ApiResponse<T> success(String msg) {
        return ApiResponse.success(msg, null);
    }

    public static <T> ApiResponse<T> error(String msg) {
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), msg);
    }

    //TokenApiResponse
    public static <T> TokenApiResponse<T> success(String msg, T data, String token) {
        return new TokenApiResponse<>(HttpStatus.OK.value(), msg, data, token);
    }
    //PaginationApiResponse
    public static <T> PaginationApiResponse<T> success(String msg, T data, Pagination pagination) {
        return new PaginationApiResponse<>(HttpStatus.OK.value(), msg, data, pagination);
    }
}

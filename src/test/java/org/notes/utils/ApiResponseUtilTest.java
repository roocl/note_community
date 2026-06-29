package org.notes.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.Pagination;
import org.notes.model.base.PaginationApiResponse;
import org.notes.model.base.TokenApiResponse;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ApiResponseUtil 响应工具测试")
class ApiResponseUtilTest {

    @Test
    @DisplayName("success(msg, data) 返回正确状态码和数据")
    void success_withMsgAndData() {
        ApiResponse<String> resp = ApiResponseUtil.success("ok", "hello");
        assertEquals(200, resp.getCode());
        assertEquals("ok", resp.getMessage());
        assertEquals("hello", resp.getData());
    }

    @Test
    @DisplayName("success(msg) 返回正确状态码, data 为 null")
    void success_withMsgOnly() {
        ApiResponse<Object> resp = ApiResponseUtil.success("ok");
        assertEquals(200, resp.getCode());
        assertEquals("ok", resp.getMessage());
        assertNull(resp.getData());
    }

    @Test
    @DisplayName("error(msg) 返回 400 状态码")
    void error_withMsg() {
        ApiResponse<Object> resp = ApiResponseUtil.error("fail");
        assertEquals(HttpStatus.BAD_REQUEST.value(), resp.getCode());
        assertEquals("fail", resp.getMessage());
    }

    @Test
    @DisplayName("success(msg, data, token) 返回 TokenApiResponse")
    void success_withToken() {
        TokenApiResponse<String> resp = ApiResponseUtil.success("登录成功", "user", "jwt-token");
        assertEquals(200, resp.getCode());
        assertEquals("登录成功", resp.getMessage());
        assertEquals("user", resp.getData());
        assertEquals("jwt-token", resp.getToken());
    }

    @Test
    @DisplayName("success(msg, data, pagination) 返回 PaginationApiResponse")
    void success_withPagination() {
        Pagination pagination = new Pagination(1, 10, 100);
        PaginationApiResponse<String> resp = ApiResponseUtil.success("列表成功", "list", pagination);
        assertEquals(200, resp.getCode());
        assertEquals("列表成功", resp.getMessage());
        assertEquals("list", resp.getData());
        assertNotNull(resp.getPagination());
        assertEquals(1, resp.getPagination().getPage());
        assertEquals(10, resp.getPagination().getPageSize());
        assertEquals(100, resp.getPagination().getTotal());
    }
}

package org.notes.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaginationUtils 分页工具测试")
class PaginationUtilsTest {

    @Test
    @DisplayName("正常计算偏移量: page=1, pageSize=10 -> 0")
    void calculateOffset_firstPage() {
        assertEquals(0, PaginationUtils.calculateOffset(1, 10));
    }

    @Test
    @DisplayName("正常计算偏移量: page=3, pageSize=20 -> 40")
    void calculateOffset_thirdPage() {
        assertEquals(40, PaginationUtils.calculateOffset(3, 20));
    }

    @Test
    @DisplayName("page < 1 时抛出 IllegalArgumentException")
    void calculateOffset_pageZero_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> PaginationUtils.calculateOffset(0, 10));
    }

    @Test
    @DisplayName("page 为负数时抛出 IllegalArgumentException")
    void calculateOffset_negPage_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> PaginationUtils.calculateOffset(-1, 10));
    }

    @Test
    @DisplayName("pageSize < 1 时抛出 IllegalArgumentException")
    void calculateOffset_pageSizeZero_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> PaginationUtils.calculateOffset(1, 0));
    }

    @Test
    @DisplayName("pageSize 为负数时抛出 IllegalArgumentException")
    void calculateOffset_negPageSize_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> PaginationUtils.calculateOffset(1, -5));
    }

    @Test
    @DisplayName("边界值: page=1, pageSize=1 -> 0")
    void calculateOffset_minValues() {
        assertEquals(0, PaginationUtils.calculateOffset(1, 1));
    }

    @Test
    @DisplayName("大页码: page=100, pageSize=50 -> 4950")
    void calculateOffset_largePage() {
        assertEquals(4950, PaginationUtils.calculateOffset(100, 50));
    }
}

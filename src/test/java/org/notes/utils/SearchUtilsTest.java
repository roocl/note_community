package org.notes.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SearchUtils 搜索工具测试")
class SearchUtilsTest {

    @Test
    @DisplayName("preprocessKeyword: null 返回空字符串")
    void preprocessKeyword_null() {
        assertEquals("", SearchUtils.preprocessKeyword(null));
    }

    @Test
    @DisplayName("preprocessKeyword: 空字符串返回空字符串")
    void preprocessKeyword_empty() {
        assertEquals("", SearchUtils.preprocessKeyword(""));
    }

    @Test
    @DisplayName("preprocessKeyword: 空白字符串返回空字符串")
    void preprocessKeyword_blank() {
        assertEquals("", SearchUtils.preprocessKeyword("   "));
    }

    @Test
    @DisplayName("preprocessKeyword: 正常关键词返回非空结果")
    void preprocessKeyword_normalKeyword() {
        String result = SearchUtils.preprocessKeyword("Java编程入门");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("preprocessKeyword: 特殊字符被去除")
    void preprocessKeyword_specialChars() {
        String result = SearchUtils.preprocessKeyword("Java!@#编程");
        assertNotNull(result);
        // 特殊字符应被替换为空格
        assertFalse(result.contains("!"));
        assertFalse(result.contains("@"));
        assertFalse(result.contains("#"));
    }

    @Test
    @DisplayName("calculateOffset: 正常计算")
    void calculateOffset_normal() {
        assertEquals(0, SearchUtils.calculateOffset(1, 10));
        assertEquals(10, SearchUtils.calculateOffset(2, 10));
        assertEquals(20, SearchUtils.calculateOffset(3, 10));
    }

    @Test
    @DisplayName("calculateOffset: page=0 返回 0 (Math.max 保护)")
    void calculateOffset_pageZero() {
        assertEquals(0, SearchUtils.calculateOffset(0, 10));
    }

    @Test
    @DisplayName("calculateOffset: 负数 page 返回 0")
    void calculateOffset_negativePage() {
        assertEquals(0, SearchUtils.calculateOffset(-1, 10));
    }
}

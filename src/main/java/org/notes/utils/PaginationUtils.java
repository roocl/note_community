package org.notes.utils;

public class PaginationUtils {
    public static int calculateOffset(int page, int pageSize) {
        if (page < 1) {
            throw new IllegalArgumentException("页码 (page) 必须大于或等于 1");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("每页大小 (pageSize) 必须大于 0");
        }
        return (page - 1) * pageSize;
    }
}

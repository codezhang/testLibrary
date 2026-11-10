package org.example.core.page;

import java.util.List;

public class PageHelper {

    public <T> List<T> page(List<T> source, int pageNum, int pageSize) {
        if (pageNum < 1) {
            throw new IllegalArgumentException("pageNum 必须大于 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize 必须大于 0");
        }
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        long fromLong = (long) (pageNum - 1) * pageSize;
        if (fromLong >= source.size()) {
            return List.of();
        }
        int from = (int) fromLong;
        int to = Math.min(from + pageSize, source.size());
        return List.copyOf(source.subList(from, to));
    }
}

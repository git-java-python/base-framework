package com.example.framework.common;

import java.util.List;

public class PageResponse<T> {

    private final long total;
    private final List<T> items;

    public PageResponse(long total, List<T> items) {
        this.total = total;
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public List<T> getItems() {
        return items;
    }
}

package com.example.graphqlshop.common;

import org.springframework.data.domain.Page;

public final class PageMapper {
    private PageMapper() {
    }

    public static PageInfo toPageInfo(Page<?> page) {
        return new PageInfo(page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.hasNext(), page.hasPrevious());
    }
}

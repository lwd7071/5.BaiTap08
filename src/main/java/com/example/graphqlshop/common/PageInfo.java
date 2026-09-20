package com.example.graphqlshop.common;

public record PageInfo(int page, int pageSize, long totalElements, int totalPages,
                       boolean hasNext, boolean hasPrevious) {
}

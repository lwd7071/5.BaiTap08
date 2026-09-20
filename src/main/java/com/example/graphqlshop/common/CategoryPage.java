package com.example.graphqlshop.common;

import com.example.graphqlshop.category.Category;
import java.util.List;

public record CategoryPage(List<Category> items, PageInfo pageInfo) {
}

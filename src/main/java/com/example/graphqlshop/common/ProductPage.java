package com.example.graphqlshop.common;

import com.example.graphqlshop.product.Product;
import java.util.List;

public record ProductPage(List<Product> items, PageInfo pageInfo) {
}

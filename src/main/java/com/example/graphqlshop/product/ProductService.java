package com.example.graphqlshop.product;

import com.example.graphqlshop.category.Category;
import com.example.graphqlshop.category.CategoryRepository;
import com.example.graphqlshop.common.GraphqlBadRequestException;
import com.example.graphqlshop.common.GraphqlNotFoundException;
import com.example.graphqlshop.common.PageInfo;
import com.example.graphqlshop.common.PageMapper;
import com.example.graphqlshop.common.ProductPage;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public ProductPage find(String keyword, Long categoryId, int page, int pageSize,
                            ProductSortField sortBy, SortDirection direction) {
        Pageable pageable = PageRequest.of(normalizePage(page), normalizePageSize(pageSize),
                Sort.by(direction.toSpringDirection(), sortBy.property()));
        Page<Product> result;
        if (categoryId != null && keyword != null && !keyword.isBlank()) {
            result = productRepository.findByNameContainingIgnoreCaseAndCategoryId(keyword.trim(), categoryId, pageable);
        } else if (categoryId != null) {
            result = productRepository.findByCategoryId(categoryId, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            result = productRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        } else {
            result = productRepository.findAll(pageable);
        }
        return new ProductPage(result.getContent(), PageMapper.toPageInfo(result));
    }

    @Transactional(readOnly = true)
    public ProductPage findByPrice(int page, int pageSize) {
        Pageable pageable = PageRequest.of(normalizePage(page), normalizePageSize(pageSize), Sort.by(Sort.Direction.ASC, "price"));
        Page<Product> result = productRepository.findAll(pageable);
        return new ProductPage(result.getContent(), PageMapper.toPageInfo(result));
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Transactional
    public Product create(ProductInput input) {
        Category category = getCategory(input.categoryId());
        validate(input);
        return productRepository.save(new Product(input.name().trim(), input.description(), input.price(),
                input.stock(), input.imageUrl(), category));
    }

    @Transactional
    public Product update(Long id, ProductInput input) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new GraphqlNotFoundException("Không tìm thấy product có id " + id));
        validate(input);
        product.setName(input.name().trim());
        product.setDescription(input.description());
        product.setPrice(input.price());
        product.setStock(input.stock());
        product.setImageUrl(input.imageUrl());
        product.setCategory(getCategory(input.categoryId()));
        return product;
    }

    @Transactional
    public boolean delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new GraphqlNotFoundException("Không tìm thấy product có id " + id);
        }
        productRepository.deleteById(id);
        return true;
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new GraphqlBadRequestException("Category không tồn tại: " + id));
    }

    private void validate(ProductInput input) {
        if (input.name() == null || input.name().isBlank()) {
            throw new GraphqlBadRequestException("Tên product không được để trống");
        }
        if (input.price() == null || input.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new GraphqlBadRequestException("Giá product phải lớn hơn hoặc bằng 0");
        }
        if (input.stock() < 0) {
            throw new GraphqlBadRequestException("Tồn kho phải lớn hơn hoặc bằng 0");
        }
    }

    private int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private int normalizePageSize(int pageSize) {
        return Math.min(Math.max(pageSize, 1), 100);
    }

    public record ProductInput(String name, String description, BigDecimal price, int stock,
                               String imageUrl, Long categoryId) {
    }

    public enum ProductSortField {
        NAME("name"), PRICE("price"), CREATED_AT("createdAt");

        private final String property;

        ProductSortField(String property) { this.property = property; }
        public String property() { return property; }
    }

    public enum SortDirection {
        ASC, DESC;

        public Sort.Direction toSpringDirection() {
            return this == ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
        }
    }
}

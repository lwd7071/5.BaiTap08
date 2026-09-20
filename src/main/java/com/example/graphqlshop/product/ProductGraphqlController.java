package com.example.graphqlshop.product;

import com.example.graphqlshop.category.Category;
import com.example.graphqlshop.common.ProductPage;
import java.util.List;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ProductGraphqlController {

    private final ProductService productService;
    private final ProductRepository productRepository;

    public ProductGraphqlController(ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    @QueryMapping
    public ProductPage products(@Argument String keyword, @Argument Long categoryId,
                                @Argument int page, @Argument int pageSize,
                                @Argument ProductService.ProductSortField sortBy,
                                @Argument ProductService.SortDirection direction) {
        return productService.find(keyword, categoryId, page, pageSize, sortBy, direction);
    }

    @QueryMapping
    public ProductPage productsByPrice(@Argument int page, @Argument int pageSize) {
        return productService.findByPrice(page, pageSize);
    }

    @QueryMapping
    public ProductPage productsByCategory(@Argument Long categoryId, @Argument int page, @Argument int pageSize) {
        return productService.find(null, categoryId, page, pageSize,
                ProductService.ProductSortField.PRICE, ProductService.SortDirection.ASC);
    }

    @QueryMapping
    public Product productById(@Argument Long id) {
        return productService.findById(id);
    }

    @MutationMapping
    public Product createProduct(@Argument ProductService.ProductInput input) {
        return productService.create(input);
    }

    @MutationMapping
    public Product updateProduct(@Argument Long id, @Argument ProductService.ProductInput input) {
        return productService.update(id, input);
    }

    @MutationMapping
    public boolean deleteProduct(@Argument Long id) {
        return productService.delete(id);
    }

    @SchemaMapping(typeName = "Category", field = "products")
    public List<Product> productsByCategory(Category category) {
        return productRepository.findByCategoryId(category.getId(), org.springframework.data.domain.PageRequest.of(0, 100,
                org.springframework.data.domain.Sort.by("name"))).getContent();
    }
}

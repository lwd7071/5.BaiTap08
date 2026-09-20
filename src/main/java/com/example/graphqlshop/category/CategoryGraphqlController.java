package com.example.graphqlshop.category;

import com.example.graphqlshop.common.CategoryPage;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class CategoryGraphqlController {

    private final CategoryService categoryService;

    public CategoryGraphqlController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @QueryMapping
    public CategoryPage categories(@Argument String keyword, @Argument int page, @Argument int pageSize) {
        return categoryService.find(keyword, page, pageSize);
    }

    @QueryMapping
    public Category categoryById(@Argument Long id) {
        return categoryService.findById(id);
    }

    @MutationMapping
    public Category createCategory(@Argument CategoryService.CategoryInput input) {
        return categoryService.create(input);
    }

    @MutationMapping
    public Category updateCategory(@Argument Long id, @Argument CategoryService.CategoryInput input) {
        return categoryService.update(id, input);
    }

    @MutationMapping
    public boolean deleteCategory(@Argument Long id) {
        return categoryService.delete(id);
    }
}

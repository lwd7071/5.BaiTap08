package com.example.graphqlshop.category;

import com.example.graphqlshop.common.CategoryPage;
import com.example.graphqlshop.common.GraphqlBadRequestException;
import com.example.graphqlshop.common.GraphqlNotFoundException;
import com.example.graphqlshop.common.PageMapper;
import com.example.graphqlshop.product.ProductRepository;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public CategoryPage find(String keyword, int page, int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(pageSize, 1), 100),
                Sort.by(Sort.Direction.ASC, "name"));
        Page<Category> result = keyword == null || keyword.isBlank()
                ? categoryRepository.findAll(pageable)
                : categoryRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        return new CategoryPage(result.getContent(), PageMapper.toPageInfo(result));
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Transactional
    public Category create(CategoryInput input) {
        validate(input);
        if (categoryRepository.existsByNameIgnoreCase(input.name().trim())) {
            throw new GraphqlBadRequestException("Tên category đã tồn tại");
        }
        Category category = new Category(input.name().trim(), input.description());
        category.touch();
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Long id, CategoryInput input) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new GraphqlNotFoundException("Không tìm thấy category có id " + id));
        validate(input);
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(input.name().trim(), id)) {
            throw new GraphqlBadRequestException("Tên category đã tồn tại");
        }
        category.setName(input.name().trim());
        category.setDescription(input.description());
        category.touch();
        return category;
    }

    @Transactional
    public boolean delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new GraphqlNotFoundException("Không tìm thấy category có id " + id);
        }
        if (productRepository.existsByCategoryId(id)) {
            throw new GraphqlBadRequestException("Không thể xóa category đang có product");
        }
        categoryRepository.deleteById(id);
        return true;
    }

    private void validate(CategoryInput input) {
        if (input.name() == null || input.name().isBlank()) {
            throw new GraphqlBadRequestException("Tên category không được để trống");
        }
    }

    public record CategoryInput(String name, String description) {
    }
}

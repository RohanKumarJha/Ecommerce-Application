package com.ecommerce.category.service.impl;

import com.ecommerce.category.dto.request.CategoryRequest;
import com.ecommerce.category.dto.response.CategoryResponse;
import com.ecommerce.category.dto.response.CategoryResponsePage;
import com.ecommerce.category.model.Category;
import com.ecommerce.category.repository.CategoryRepository;
import com.ecommerce.category.service.CategoryService;
import com.ecommerce.core.exception.APIException;
import com.ecommerce.core.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // ---------------------- GET ALL (TREE) ----------------------
    @Override
    @Transactional
    public CategoryResponsePage getAllCategories(
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder) {
        logger.info("Fetching all categories | page: {}, size: {}, sortBy: {}, sortOrder: {}",
                pageNumber, pageSize, sortBy, sortOrder);
        Sort sort = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        if (rootCategories.isEmpty()) {
            logger.warn("No categories found");
            throw new APIException("No categories found");
        }
        List<CategoryResponse> content = rootCategories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        logger.info("Fetched {} root categories", content.size());
        return CategoryResponsePage.builder()
                .content(content)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalRootCategories((long) content.size())
                .totalCategories(countAllCategories(content))
                .totalPages(1)
                .lastPage(true)
                .build();
    }

    // ---------------------- CREATE ----------------------
    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        logger.info("Creating category: {}", request.getCategoryName());
        Category existing = categoryRepository.findByCategoryName(request.getCategoryName());
        if (existing != null) {
            logger.error("Category already exists: {}", request.getCategoryName());
            throw new APIException("Category already exists");
        }
        Category parent = null;
        if (request.getParentId() != null) {
            logger.info("Fetching parent category: {}", request.getParentId());
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> {
                        logger.error("Parent category not found: {}", request.getParentId());
                        return new ResourceNotFoundException("Category", "id", request.getParentId());
                    });
        }
        Category category = Category.builder()
                .categoryName(request.getCategoryName())
                .parent(parent)
                .build();
        Category saved = categoryRepository.save(category);
        logger.info("Category created with id: {}", saved.getCategoryId());
        return mapToResponse(saved);
    }

    // ---------------------- DELETE ----------------------
    @Override
    public CategoryResponse deleteCategory(Long categoryId) {
        logger.info("Deleting category id: {}", categoryId);
        Category category = getCategoryOrThrow(categoryId);
        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            logger.error("Cannot delete category {} with subcategories", categoryId);
            throw new APIException("Cannot delete category with subcategories");
        }
        categoryRepository.delete(category);
        logger.info("Category deleted: {}", categoryId);
        return mapToResponse(category);
    }

    // ---------------------- UPDATE ----------------------
    @Override
    public CategoryResponse updateCategory(CategoryRequest request, Long categoryId) {
        logger.info("Updating category id: {}", categoryId);
        Category category = getCategoryOrThrow(categoryId);
        category.setCategoryName(request.getCategoryName());
        if (request.getParentId() != null) {
            logger.info("Updating parent for category {} to {}", categoryId, request.getParentId());
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> {
                        logger.error("Parent category not found: {}", request.getParentId());
                        return new ResourceNotFoundException("Category", "id", request.getParentId());
                    });
            validateNoCycle(category, parent);
            category.setParent(parent);
        }
        Category saved = categoryRepository.save(category);
        logger.info("Category updated: {}", saved.getCategoryId());
        return mapToResponse(saved);
    }

    // ---------------------- HELPER ----------------------

    private Category getCategoryOrThrow(Long id) {
        logger.debug("Fetching category id: {}", id);
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Category not found: {}", id);
                    return new ResourceNotFoundException("Category", "categoryId", id);
                });
    }

    private CategoryResponse mapToResponse(Category category) {
        List<CategoryResponse> subCategories =
                category.getSubCategories() == null
                        ? List.of()
                        : category.getSubCategories()
                          .stream()
                          .map(this::mapToResponse)
                          .toList();
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .subCategories(subCategories)
                .build();
    }

    private void validateNoCycle(Category category, Category parent) {
        while (parent != null) {
            if (parent.getCategoryId().equals(category.getCategoryId())) {
                logger.error("Cycle detected for category id: {}", category.getCategoryId());
                throw new APIException("Cycle detected in category hierarchy");
            }
            parent = parent.getParent();
        }
    }

    private Long countAllCategories(List<CategoryResponse> categories) {
        long count = 0;
        for (CategoryResponse category : categories) {
            count++;
            count += countAllCategories(category.getSubCategories());
        }
        return count;
    }
}
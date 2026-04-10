package com.ecommerce.category.service;

import com.ecommerce.category.dto.request.CategoryRequest;
import com.ecommerce.category.dto.response.CategoryResponse;
import com.ecommerce.category.dto.response.CategoryResponsePage;

public interface CategoryService {

    CategoryResponsePage getAllCategories(
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder);

    CategoryResponse createCategory(CategoryRequest categoryRequest);

    CategoryResponse deleteCategory(Long categoryId);

    CategoryResponse updateCategory(CategoryRequest categoryRequest, Long categoryId);
}
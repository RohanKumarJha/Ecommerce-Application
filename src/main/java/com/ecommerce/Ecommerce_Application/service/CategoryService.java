package com.ecommerce.Ecommerce_Application.service;

import com.ecommerce.Ecommerce_Application.payload.CategoryRequest;
import com.ecommerce.Ecommerce_Application.payload.CategoryResponse;
import com.ecommerce.Ecommerce_Application.payload.CategoryResponsePage;

public interface CategoryService {
    CategoryResponsePage getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    CategoryResponse createCategory(CategoryRequest categoryDTO);
    CategoryResponse deleteCategory(Long categoryId);
    CategoryResponse updateCategory(CategoryRequest categoryDTO, Long categoryId);
}
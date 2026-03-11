package com.ecommerce.Ecommerce_Application.service;

import com.ecommerce.Ecommerce_Application.payload.request.CategoryRequest;
import com.ecommerce.Ecommerce_Application.payload.response.CategoryResponse;
import com.ecommerce.Ecommerce_Application.payload.response.CategoryResponsePage;

public interface CategoryService {
    CategoryResponsePage getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    CategoryResponse createCategory(CategoryRequest categoryDTO);
    CategoryResponse deleteCategory(Long categoryId);
    CategoryResponse updateCategory(CategoryRequest categoryDTO, Long categoryId);
}
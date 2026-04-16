package ecommerce.category.service;

import ecommerce.category.dto.request.CategoryRequest;
import ecommerce.category.dto.response.CategoryResponse;
import ecommerce.core.dto.response.MessageResponse;
import ecommerce.core.dto.response.PagedResponse;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(CategoryRequest request, Long categoryId);

    MessageResponse deleteCategory(Long categoryId);

    PagedResponse<CategoryResponse> getAllCategories(Integer page, Integer size, String sortBy, String sortDir);
}

package com.ecommerce.category.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryResponse {
    private Long categoryId;
    private String categoryName;
    private List<CategoryResponse> subCategories;
}
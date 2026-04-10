package com.ecommerce.category.dto.request;

import lombok.Data;

@Data
public class CategoryRequest {
    private String categoryName;
    private Long parentId;
}
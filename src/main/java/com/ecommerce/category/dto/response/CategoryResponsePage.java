package com.ecommerce.category.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryResponsePage {

    private List<CategoryResponse> content;

    private Integer pageNumber;
    private Integer pageSize;

    private Long totalRootCategories;

    private Long totalCategories;

    private Integer totalPages;
    private boolean lastPage;
}
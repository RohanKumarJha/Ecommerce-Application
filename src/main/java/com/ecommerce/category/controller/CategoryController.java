package com.ecommerce.category.controller;

import com.ecommerce.category.dto.request.CategoryRequest;
import com.ecommerce.category.dto.response.CategoryResponse;
import com.ecommerce.category.dto.response.CategoryResponsePage;
import com.ecommerce.category.service.CategoryService;
import com.ecommerce.core.util.AppConstants;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api") // Base API path
public class CategoryController {

    private final Logger log = LoggerFactory.getLogger(CategoryController.class); // Logger

    @Autowired
    private CategoryService categoryService; // Category service

    @GetMapping("/public/categories")
    public ResponseEntity<CategoryResponsePage> getAllCategories(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_CATEGORIES_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        log.info("Fetching categories: pageNumber={}, pageSize={}, sortBy={}, sortOrder={}",
                pageNumber, pageSize, sortBy, sortOrder); // Fetch categories with pagination
        CategoryResponsePage categoryResponse = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder);
        log.info("Fetched {} categories", categoryResponse.getContent().size());
        return new ResponseEntity<>(categoryResponse, HttpStatus.OK); // Return 200
    }

    @PostMapping("/admin/categories")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest categoryRequest){
        log.info("Creating new category with name: {}", categoryRequest.getCategoryName()); // Create category
        CategoryResponse savedCategory = categoryService.createCategory(categoryRequest);
        log.info("Category created with ID: {}", savedCategory.getCategoryId());
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED); // Return 201
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryResponse> deleteCategory(@PathVariable Long categoryId){
        log.info("Deleting category with ID: {}", categoryId); // Delete category
        CategoryResponse deletedCategory = categoryService.deleteCategory(categoryId);
        log.info("Category deleted with ID: {}", deletedCategory.getCategoryId());
        return new ResponseEntity<>(deletedCategory, HttpStatus.OK); // Return 200
    }

    @PutMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(@Valid @RequestBody CategoryRequest categoryRequest,
                                                           @PathVariable Long categoryId){
        log.info("Updating category with ID: {}, new name: {}", categoryId, categoryRequest.getCategoryName()); // Update category
        CategoryResponse updatedCategory = categoryService.updateCategory(categoryRequest, categoryId);
        log.info("Category updated with ID: {}", updatedCategory.getCategoryId());
        return new ResponseEntity<>(updatedCategory, HttpStatus.OK); // Return 200
    }
}
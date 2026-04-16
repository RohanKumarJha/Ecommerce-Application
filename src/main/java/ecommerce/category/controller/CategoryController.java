package ecommerce.category.controller;

import ecommerce.category.dto.request.CategoryRequest;
import ecommerce.category.dto.response.CategoryResponse;
import ecommerce.category.service.CategoryService;
import ecommerce.core.dto.response.MessageResponse;
import ecommerce.core.dto.response.PagedResponse;
import ecommerce.core.util.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    @GetMapping
    public ResponseEntity<PagedResponse<CategoryResponse>> getAll(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer page,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer size,
            @RequestParam(defaultValue = AppConstants.SORT_CATEGORIES_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR) String sortDir) {

        return ResponseEntity.ok(service.getAllCategories(page, size, sortBy, sortDir));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(201).body(service.createCategory(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        return ResponseEntity.ok(service.updateCategory(request, id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.deleteCategory(id));
    }
}
package ecommerce.product.controller;

import ecommerce.core.dto.response.MessageResponse;
import ecommerce.core.dto.response.PagedResponse;
import ecommerce.core.util.AppConstants;
import ecommerce.product.dto.request.ProductRequest;
import ecommerce.product.dto.response.ProductResponse;
import ecommerce.product.service.ProductService;
import ecommerce.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    // ===================== CREATE PRODUCT =====================
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @PostMapping("/category/{categoryId}")
    public ResponseEntity<ProductResponse> create(
            @PathVariable Long categoryId,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserDetailsImpl user) {

        return ResponseEntity.status(201)
                .body(service.addProduct(categoryId, request, user.getUserId()));
    }

    // ===================== GET ALL PRODUCTS =====================
    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> getAll(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer page,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer size,
            @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR) String sortDir) {

        return ResponseEntity.ok(service.getAllProducts(page, size, sortBy, sortDir));
    }

    // ===================== GET PRODUCT BY ID =====================
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getProductById(id));
    }

    // ===================== DELETE PRODUCT =====================
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.deleteProduct(id));
    }

    // ===================== UPDATE PRODUCT =====================
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        return ResponseEntity.ok(service.updateProduct(id, request));
    }

    // ===================== SEARCH PRODUCTS BY KEYWORD =====================
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<ProductResponse>> searchByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer page,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer size,
            @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR) String sortDir) {

        return ResponseEntity.ok(
                service.searchProductByKeyword(keyword, page, size, sortBy, sortDir)
        );
    }

    // ===================== GET PRODUCTS BY CATEGORY =====================
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<PagedResponse<ProductResponse>> getByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer page,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer size,
            @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR) String sortDir) {

        return ResponseEntity.ok(
                service.searchByCategory(categoryId, page, size, sortBy, sortDir)
        );
    }

    // ===================== UPDATE PRODUCT IMAGE =====================
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> updateImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) throws IOException {

        return ResponseEntity.ok(service.updateProductImage(id, image));
    }

}
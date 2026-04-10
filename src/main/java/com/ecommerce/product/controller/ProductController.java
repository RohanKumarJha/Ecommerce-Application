package com.ecommerce.product.controller;

import com.ecommerce.core.security.services.UserDetailsImpl;
import com.ecommerce.core.util.AppConstants;
import com.ecommerce.product.dto.request.ProductRequest;
import com.ecommerce.product.dto.response.ProductResponse;
import com.ecommerce.product.dto.response.ProductResponsePage;
import com.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api") // Base API path
public class ProductController {

    private final Logger log = LoggerFactory.getLogger(ProductController.class); // Logger

    @Autowired
    private ProductService productService; // Product service

    @PostMapping("/admin/categories/{categoryId}/product")
    public ResponseEntity<ProductResponse> addProduct(
            @Valid @RequestBody ProductRequest productRequest,
            @PathVariable Long categoryId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUserId(); // Get logged-in userId
        log.info("User ID {} adding product '{}' to category ID {}", userId, productRequest.getProductName(), categoryId);
        ProductResponse savedProduct = productService.addProduct(categoryId, productRequest, userId);
        log.info("Product added with ID {}", savedProduct.getProductId());
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED); // Return 201
    }

    @GetMapping("/public/products")
    public ResponseEntity<ProductResponsePage> getAllProducts(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        log.info("Fetching all products: pageNumber={}, pageSize={}, sortBy={}, sortOrder={}",
                pageNumber, pageSize, sortBy, sortOrder); // Fetch all products
        ProductResponsePage productResponse = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder);
        log.info("Fetched {} products", productResponse.getContent().size());
        return new ResponseEntity<>(productResponse,HttpStatus.OK); // Return 200
    }

    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponsePage> getProductsByCategory(@PathVariable Long categoryId,
                                                                     @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                                     @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                                     @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
                                                                     @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder){
        log.info("Fetching products for category ID {}: pageNumber={}, pageSize={}, sortBy={}, sortOrder={}",
                categoryId, pageNumber, pageSize, sortBy, sortOrder); // Fetch by category
        ProductResponsePage productResponse = productService.searchByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder);
        log.info("Fetched {} products for category ID {}", productResponse.getContent().size(), categoryId);
        return new ResponseEntity<>(productResponse, HttpStatus.OK); // Return 200
    }

    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponsePage> getProductsByKeyword(@PathVariable String keyword,
                                                                    @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                                    @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                                    @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
                                                                    @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder){
        log.info("Searching products by keyword '{}': pageNumber={}, pageSize={}, sortBy={}, sortOrder={}",
                keyword, pageNumber, pageSize, sortBy, sortOrder); // Search products
        ProductResponsePage productResponse = productService.searchProductByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder);
        log.info("Found {} products for keyword '{}'", productResponse.getContent().size(), keyword);
        return new ResponseEntity<>(productResponse, HttpStatus.FOUND); // Return 302
    }

    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(@Valid @RequestBody ProductRequest productRequest,
                                                         @PathVariable Long productId){
        log.info("Updating product ID {} with new name '{}'", productId, productRequest.getProductName()); // Update product
        ProductResponse updatedProduct = productService.updateProduct(productId, productRequest);
        log.info("Product updated with ID {}", updatedProduct.getProductId());
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK); // Return 200
    }

    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<ProductResponse> deleteProduct(@PathVariable Long productId){
        log.info("Deleting product with ID {}", productId); // Delete product
        ProductResponse deletedProduct = productService.deleteProduct(productId);
        log.info("Deleted product ID {}", deletedProduct.getProductId());
        return new ResponseEntity<>(deletedProduct, HttpStatus.OK); // Return 200
    }

    @PutMapping("/admin/products/{productId}/image")
    public ResponseEntity<ProductResponse> updateProductImage(
            @PathVariable Long productId,
            @RequestParam("image") MultipartFile image) throws IOException {
        log.info("Updating image for productId={}", productId); // Update product image
        ProductResponse updatedProduct = productService.updateProductImage(productId, image);
        log.info("Product image updated for productId={}", productId);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK); // Return 200
    }
}
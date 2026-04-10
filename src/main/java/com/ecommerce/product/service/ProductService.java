package com.ecommerce.product.service;

import com.ecommerce.product.dto.request.ProductRequest;
import com.ecommerce.product.dto.response.ProductResponse;
import com.ecommerce.product.dto.response.ProductResponsePage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {
    ProductResponse addProduct(Long categoryId, ProductRequest product, Long userId);

    ProductResponsePage getAllProducts(
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder);

    ProductResponsePage searchByCategory(
            Long categoryId,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder);

    ProductResponsePage searchProductByKeyword(
            String keyword,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder);

    ProductResponse updateProduct(Long productId, ProductRequest product);

    ProductResponse deleteProduct(Long productId);

    ProductResponse updateProductImage(Long productId, MultipartFile image) throws IOException;
}
package com.ecommerce.Ecommerce_Application.service;

import com.ecommerce.Ecommerce_Application.payload.ProductRequest;
import com.ecommerce.Ecommerce_Application.payload.ProductResponse;
import com.ecommerce.Ecommerce_Application.payload.ProductResponsePage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {
    ProductResponse addProduct(Long categoryId, ProductRequest product);
    ProductResponsePage getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    ProductResponsePage searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    ProductResponsePage searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    ProductResponse updateProduct(Long productId, ProductRequest product);
    ProductResponse deleteProduct(Long productId);
    ProductResponse updateProductImage(Long productId, MultipartFile image) throws IOException;
}

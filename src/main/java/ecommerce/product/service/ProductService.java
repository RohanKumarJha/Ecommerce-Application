package ecommerce.product.service;

import ecommerce.core.dto.response.PagedResponse;
import ecommerce.product.dto.request.ProductRequest;
import ecommerce.product.dto.response.ProductResponse;
import ecommerce.core.dto.response.MessageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {

    ProductResponse addProduct(Long categoryId, ProductRequest request, Long sellerId);

    PagedResponse<ProductResponse> getAllProducts(Integer page, Integer size, String sortBy, String sortDir);

    PagedResponse<ProductResponse> searchByCategory(Long categoryId, Integer page, Integer size, String sortBy, String sortDir);

    PagedResponse<ProductResponse> searchProductByKeyword(String keyword, Integer page, Integer size, String sortBy, String sortDir);

    ProductResponse getProductById(Long productId);

    ProductResponse updateProduct(Long productId, ProductRequest request);

    MessageResponse deleteProduct(Long productId);

    ProductResponse updateProductImage(Long productId, MultipartFile image) throws IOException;
}
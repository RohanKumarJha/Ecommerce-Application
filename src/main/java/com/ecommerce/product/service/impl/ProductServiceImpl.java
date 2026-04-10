package com.ecommerce.product.service.impl;

import com.ecommerce.category.model.Category;
import com.ecommerce.category.repository.CategoryRepository;
import com.ecommerce.core.exception.ResourceNotFoundException;
import com.ecommerce.product.dto.request.ProductRequest;
import com.ecommerce.product.dto.response.ProductResponse;
import com.ecommerce.product.dto.response.ProductResponsePage;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.FileService;
import com.ecommerce.product.service.ProductService;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final FileService fileService;

    @Value("${project.image}")
    private String path;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              UserRepository userRepository,
                              ModelMapper modelMapper,
                              FileService fileService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.fileService = fileService;
    }

    // ------------------- ADD PRODUCT -------------------
    @Override
    public ProductResponse addProduct(Long categoryId, ProductRequest productRequest, Long userId) {
        log.info("Adding product: {} for categoryId: {} by userId: {}",
                productRequest.getProductName(), categoryId, userId);
        Product product = buildProductFromRequest(productRequest);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category not found: {}", categoryId);
                    return new ResourceNotFoundException("Category", "categoryId", categoryId);
                });
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new ResourceNotFoundException("User", "userId", userId);
                });
        product.setCategory(category);
        product.setUser(user);
        Product saved = productRepository.save(product);
        log.info("Product created with id: {}", saved.getProductId());
        return mapToProductResponse(saved);
    }

    // ------------------- GET ALL PRODUCTS -------------------
    @Override
    public ProductResponsePage getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        log.info("Fetching all products | page: {}, size: {}, sortBy: {}, sortOrder: {}",
                pageNumber, pageSize, sortBy, sortOrder);
        Pageable pageable = createPageable(pageNumber, pageSize, sortBy, sortOrder);
        Page<Product> page = productRepository.findAll(pageable);
        log.info("Fetched {} products", page.getTotalElements());
        return mapToProductResponsePage(page);
    }

    // ------------------- SEARCH BY CATEGORY -------------------
    @Override
    public ProductResponsePage searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize,
                                                String sortBy, String sortOrder) {
        log.info("Searching products by categoryId: {}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category not found: {}", categoryId);
                    return new ResourceNotFoundException("Category", "categoryId", categoryId);
                });
        Pageable pageable = createPageable(pageNumber, pageSize, sortBy, sortOrder);
        Page<Product> page = productRepository.findByCategory(category, pageable);
        log.info("Found {} products for categoryId: {}", page.getTotalElements(), categoryId);
        return mapToProductResponsePage(page);
    }

    // ------------------- SEARCH BY KEYWORD -------------------
    @Override
    public ProductResponsePage searchProductByKeyword(String keyword, Integer pageNumber,
                                                      Integer pageSize, String sortBy, String sortOrder) {
        log.info("Searching products by keyword: {}", keyword);
        Pageable pageable = createPageable(pageNumber, pageSize, sortBy, sortOrder);
        Page<Product> page = productRepository
                .findByProductNameContainingIgnoreCase(keyword, pageable);
        log.info("Found {} products for keyword: {}", page.getTotalElements(), keyword);
        return mapToProductResponsePage(page);
    }

    // ------------------- UPDATE PRODUCT -------------------
    @Override
    public ProductResponse updateProduct(Long productId, ProductRequest productRequest) {
        log.info("Updating product id: {}", productId);
        Product existing = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Product not found: {}", productId);
                    return new ResourceNotFoundException("Product", "productId", productId);
                });
        BigDecimal discount = productRequest.getDiscount() != null
                ? productRequest.getDiscount()
                : BigDecimal.ZERO;
        BigDecimal specialPrice = productRequest.getPrice().subtract(discount);
        Product updated = Product.builder()
                .productId(existing.getProductId())
                .productName(productRequest.getProductName())
                .description(productRequest.getDescription())
                .quantity(productRequest.getQuantity())
                .price(productRequest.getPrice())
                .discount(discount)
                .specialPrice(specialPrice)
                .image(existing.getImage())
                .category(existing.getCategory())
                .user(existing.getUser())
                .build();
        Product saved = productRepository.save(updated);
        log.info("Product updated successfully: {}", saved.getProductId());
        return mapToProductResponse(saved);
    }

    // ------------------- DELETE PRODUCT -------------------
    @Override
    public ProductResponse deleteProduct(Long productId) {
        log.info("Deleting product id: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Product not found: {}", productId);
                    return new ResourceNotFoundException("Product", "productId", productId);
                });
        productRepository.delete(product);
        log.info("Product deleted: {}", productId);
        return mapToProductResponse(product);
    }

    // ------------------- UPDATE IMAGE -------------------
    @Override
    public ProductResponse updateProductImage(Long productId, MultipartFile image) throws IOException {
        log.info("Updating image for product id: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Product not found: {}", productId);
                    return new ResourceNotFoundException("Product", "productId", productId);
                });
        String fileName = fileService.uploadImage(path, image);
        log.info("Image uploaded successfully: {}", fileName);
        product.setImage(fileName);
        Product updated = productRepository.save(product);
        return modelMapper.map(updated, ProductResponse.class);
    }

    // ------------------- HELPER METHODS -------------------

    private Product buildProductFromRequest(ProductRequest request) {
        BigDecimal discount = request.getDiscount() != null
                ? request.getDiscount()
                : BigDecimal.ZERO;
        BigDecimal specialPrice = request.getPrice().subtract(discount);
        return Product.builder()
                .productName(request.getProductName())
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .discount(discount)
                .specialPrice(specialPrice)
                .image(request.getImage())
                .build();
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discount(product.getDiscount())
                .specialPrice(product.getSpecialPrice())
                .quantity(product.getQuantity())
                .image(product.getImage())
                .categoryId(product.getCategory().getCategoryId())
                .build();
    }

    private ProductResponsePage mapToProductResponsePage(Page<Product> page) {
        List<ProductResponse> content = page.getContent()
                .stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
        return ProductResponsePage.builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .lastPage(page.isLast())
                .build();
    }

    private Pageable createPageable(Integer pageNumber, Integer pageSize,
                                    String sortBy, String sortOrder) {
        Sort sort = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(pageNumber, pageSize, sort);
    }
}
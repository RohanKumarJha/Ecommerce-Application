package ecommerce.product.service.impl;

import ecommerce.category.repository.CategoryRepository;
import ecommerce.core.dto.response.MessageResponse;
import ecommerce.core.dto.response.PagedResponse;
import ecommerce.core.exception.APIException;
import ecommerce.core.exception.ResourceNotFoundException;
import ecommerce.product.dto.request.ProductRequest;
import ecommerce.product.dto.response.ProductResponse;
import ecommerce.product.model.Product;
import ecommerce.product.repository.ProductRepository;
import ecommerce.product.service.FileService;
import ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final FileService fileService;

    @Value("${project.image}")
    private String path;

    @Override
    @Transactional
    public ProductResponse addProduct(Long categoryId, ProductRequest request, Long sellerId) {
        log.debug("Adding product '{}' for categoryId={} by sellerId={}",
                request.getProductName(), categoryId, sellerId);
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "categoryId", categoryId);
        }
        Product product = modelMapper.map(request, Product.class);
        product.setCategoryId(categoryId);
        product.setSellerId(sellerId);
        if (product.getDiscount() == null) {
            product.setDiscount(BigDecimal.ZERO);
        }
        Product saved = productRepository.save(product);
        log.info("Product created successfully id={} for sellerId={}",
                saved.getProductId(), sellerId);
        return modelMapper.map(saved, ProductResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getAllProducts(
            Integer page, Integer size, String sortBy, String sortDir) {
        log.debug("Fetching products page={}, size={}, sortBy={}, sortDir={}",
                page, size, sortBy, sortDir);
        page = (page == null) ? 0 : page;
        size = (size == null) ? 10 : size;
        sortBy = (sortBy == null) ? "productId" : sortBy;
        sortDir = (sortDir == null) ? "asc" : sortDir;
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findAll(pageable);
        List<ProductResponse> content = productPage.getContent()
                .stream()
                .map(product -> modelMapper.map(product, ProductResponse.class))
                .toList();
        log.info("Fetched {} products (total={})",
                content.size(),
                productPage.getTotalElements());
        return PagedResponse.<ProductResponse>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .lastPage(productPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> searchByCategory(
            Long categoryId, Integer page, Integer size, String sortBy, String sortDir) {
        log.debug("Fetching products for categoryId={}, page={}, size={}",
                categoryId, page, size);
        page = (page == null) ? 0 : page;
        size = (size == null) ? 10 : size;
        sortBy = (sortBy == null) ? "productId" : sortBy;
        sortDir = (sortDir == null) ? "asc" : sortDir;
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "categoryId", categoryId);
        }
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage =
                productRepository.findByCategoryId(categoryId, pageable);
        List<ProductResponse> content = productPage.getContent()
                .stream()
                .map(product -> modelMapper.map(product, ProductResponse.class))
                .toList();
        log.info("Found {} products (total={}) for categoryId={}",
                content.size(),
                productPage.getTotalElements(),
                categoryId);
        return PagedResponse.<ProductResponse>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .lastPage(productPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> searchProductByKeyword(
            String keyword, Integer page, Integer size, String sortBy, String sortDir) {
        log.debug("Searching products with keyword='{}', page={}, size={}",
                keyword, page, size);
        page = (page == null) ? 0 : page;
        size = (size == null) ? 10 : size;
        sortBy = (sortBy == null) ? "productId" : sortBy;
        sortDir = (sortDir == null) ? "asc" : sortDir;
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new APIException("Keyword must not be empty");
        }
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage =
                productRepository.findByProductNameContainingIgnoreCase(keyword, pageable);
        List<ProductResponse> content = productPage.getContent()
                .stream()
                .map(product -> modelMapper.map(product, ProductResponse.class))
                .toList();
        log.info("Search result: {} products found (total={}) for keyword='{}'",
                content.size(),
                productPage.getTotalElements(),
                keyword);
        return PagedResponse.<ProductResponse>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .lastPage(productPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId) {
        log.debug("Fetching product with id={}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", "productId", productId
                ));
        log.info("Product found with id={}", productId);
        return modelMapper.map(product, ProductResponse.class);
    }

    @Override
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        log.debug("Updating product id={}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", "productId", productId
                ));
        if (productRepository.existsByProductNameAndProductIdNot(
                request.getProductName(), productId)) {
            log.error("Product name '{}' already exists", request.getProductName());
            throw new APIException("Product already exists with name: "
                    + request.getProductName());
        }
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setImage(request.getImage());
        product.setPrice(request.getPrice());
        if (request.getDiscount() != null) {
            product.setDiscount(request.getDiscount());
        }
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully id={}", productId);
        return modelMapper.map(updatedProduct, ProductResponse.class);
    }

    @Override
    @Transactional
    public MessageResponse deleteProduct(Long productId) {
        log.debug("Soft deleting product id={}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", "productId", productId
                ));
        productRepository.delete(product);
        log.info("Product deleted successfully id={}", productId);
        return new MessageResponse("Product deleted successfully");
    }

    @Override
    public ProductResponse updateProductImage(Long productId, MultipartFile image) throws IOException {
        log.info("Starting image update for productId={}", productId);

        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found for productId={}", productId);
                    return new ResourceNotFoundException("Product", "productId", productId);
                });

        String fileName = fileService.uploadImage(path, image);
        log.info("Image uploaded successfully for productId={}, fileName={}", productId, fileName);

        productFromDb.setImage(fileName);
        Product updatedProduct = productRepository.save(productFromDb);
        log.info("Product image updated successfully for productId={}", productId);

        return modelMapper.map(updatedProduct, ProductResponse.class);
    }
}

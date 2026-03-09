package com.ecommerce.Ecommerce_Application.service;

import com.ecommerce.Ecommerce_Application.exception.APIException;
import com.ecommerce.Ecommerce_Application.exception.ResourceNotFoundException;
import com.ecommerce.Ecommerce_Application.model.Category;
import com.ecommerce.Ecommerce_Application.model.Product;
import com.ecommerce.Ecommerce_Application.payload.ProductRequest;
import com.ecommerce.Ecommerce_Application.payload.ProductResponse;
import com.ecommerce.Ecommerce_Application.payload.ProductResponsePage;
import com.ecommerce.Ecommerce_Application.repository.CategoryRepository;
import com.ecommerce.Ecommerce_Application.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    @Override
    public ProductResponse addProduct(Long categoryId, ProductRequest productRequest) {

        Category category = categoryRepository.findById(categoryId).orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId));
        boolean isProductNotPresent = true;
        List<Product> products = category.getProducts();
        for (Product value : products) {
            if (value.getProductName().equals(productRequest.getProductName())) {
                isProductNotPresent = false;
                break;
            }
        }

        if (isProductNotPresent) {
            Product product = modelMapper.map(productRequest, Product.class);
            product.setImage("default.png");
            product.setCategory(category);
            double specialPrice = product.getPrice() -
                    ((product.getDiscount() * 0.01) * product.getPrice());
            product.setSpecialPrice(specialPrice);
            Product savedProduct = productRepository.save(product);
            return modelMapper.map(savedProduct, ProductResponse.class);
        } else {
            throw new APIException("Product already exists!");
        }
    }



    @Override
    public ProductResponsePage getAllProducts(
            Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> pageProducts = productRepository.findAll(pageDetails);

        List<Product> products = pageProducts.getContent();

        List<ProductResponse> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductResponse.class))
                .toList();

        ProductResponsePage productResponsePage = new ProductResponsePage();

        productResponsePage.setContent(productDTOS);
        productResponsePage.setPageNumber(pageProducts.getNumber());
        productResponsePage.setPageSize(pageProducts.getSize());
        productResponsePage.setTotalElements(pageProducts.getTotalElements());
        productResponsePage.setTotalPages(pageProducts.getTotalPages());
        productResponsePage.setLastPage(pageProducts.isLast());

        return productResponsePage;
    }



    @Override
    public ProductResponsePage searchByCategory(
            Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId));

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByCategoryOrderByPriceAsc(category, pageDetails);

        List<Product> products = pageProducts.getContent();

        if(products.isEmpty()){
            throw new APIException(category.getCategoryName() + " category does not have any products");
        }

        List<ProductResponse> productDTOS = products.stream().map(product ->
                        modelMapper.map(product, ProductResponse.class)).toList();

        ProductResponsePage productResponse = new ProductResponsePage();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;
    }



    @Override
    public ProductResponsePage searchProductByKeyword(
            String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts =
                productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%', pageDetails);

        List<Product> products = pageProducts.getContent();
        List<ProductResponse> productDTOS = products.stream().map(product ->
                        modelMapper.map(product, ProductResponse.class)).toList();

        if(products.isEmpty()){
            throw new APIException("Products not found with keyword: " + keyword);
        }

        ProductResponsePage productResponse = new ProductResponsePage();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;
    }



    @Override
    public ProductResponse updateProduct(Long productId, ProductRequest productDTO) {
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        Product product = modelMapper.map(productDTO, Product.class);

        productFromDb.setProductName(product.getProductName());
        productFromDb.setDescription(product.getDescription());
        productFromDb.setQuantity(product.getQuantity());
        productFromDb.setDiscount(product.getDiscount());
        productFromDb.setPrice(product.getPrice());
        productFromDb.setSpecialPrice(product.getSpecialPrice());

        Product savedProduct = productRepository.save(productFromDb);

//        List<Cart> carts = cartRepository.findCartsByProductId(productId);
//
//        List<CartDTO> cartDTO = carts.stream().map(cart -> {
//            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
//
//            List<ProductDTO> products = cart.getCartItems().stream()
//                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());
//
//            cartDTO.setProducts(products);
//
//            return cartDTO;
//
//        }).collect(Collectors.toList());
//
//        cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));

        return modelMapper.map(savedProduct, ProductResponse.class);
    }



    @Override
    public ProductResponse deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // DELETE
//        List<Cart> carts = cartRepository.findCartsByProductId(productId);
//        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));

        productRepository.delete(product);
        return modelMapper.map(product, ProductResponse.class);
    }



    @Override
    public ProductResponse updateProductImage(Long productId, MultipartFile image) throws IOException {
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        String fileName = fileService.uploadImage(path, image);
        productFromDb.setImage(fileName);

        Product updatedProduct = productRepository.save(productFromDb);
        return modelMapper.map(updatedProduct, ProductResponse.class);
    }
}

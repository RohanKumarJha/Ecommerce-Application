package ecommerce.category.service.impl;

import ecommerce.category.dto.request.CategoryRequest;
import ecommerce.category.dto.response.CategoryResponse;
import ecommerce.category.model.Category;
import ecommerce.category.repository.CategoryRepository;
import ecommerce.category.service.CategoryService;
import ecommerce.core.dto.response.MessageResponse;
import ecommerce.core.dto.response.PagedResponse;
import ecommerce.core.exception.APIException;
import ecommerce.core.exception.ResourceNotFoundException;
import ecommerce.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.debug("Creating category with name={}", request.getCategoryName());
        Optional<Category> existing = categoryRepository.findByCategoryName(request.getCategoryName());
        if (existing.isPresent())
            throw new APIException("Category already exists with name: " + request.getCategoryName());
        Category category = modelMapper.map(request, Category.class);
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with id={}", savedCategory.getCategoryId());
        return modelMapper.map(savedCategory, CategoryResponse.class);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(CategoryRequest request, Long categoryId) {
        log.debug("Updating category id={} with name={}", categoryId, request.getCategoryName());
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category", "categoryId", categoryId));
        Optional<Category> existing = categoryRepository.findByCategoryName(request.getCategoryName());
        if (existing.isPresent() &&
                !existing.get().getCategoryId().equals(categoryId)) {
            throw new APIException("Category already exists with name: " + request.getCategoryName());
        }
        category.setCategoryName(request.getCategoryName());
        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully id={}", updatedCategory.getCategoryId());
        return modelMapper.map(updatedCategory, CategoryResponse.class);
    }

    @Override
    @Transactional
    public MessageResponse deleteCategory(Long categoryId) {
        log.debug("Deleting category id={}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category", "categoryId", categoryId));
        if (productRepository.existsByCategoryId(categoryId)) {
            throw new APIException("Cannot delete category. It is already assigned to products.");
        }
        categoryRepository.delete(category);
        log.info("Category deleted successfully id={}", categoryId);
        return new MessageResponse("Category deleted successfully");
    }

    @Override
    @Transactional
    public PagedResponse<CategoryResponse> getAllCategories(
            Integer page, Integer size, String sortBy, String sortDir) {
        log.debug("Fetching categories page={}, size={}, sortBy={}, sortDir={}",
                page, size, sortBy, sortDir);
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        List<CategoryResponse> content = categoryPage.getContent()
                .stream()
                .map(category -> modelMapper.map(category, CategoryResponse.class))
                .toList();
        log.info("Fetched {} categories out of total={}",
                content.size(), categoryPage.getTotalElements());
        return new PagedResponse<>(
                content,
                categoryPage.getNumber(),
                categoryPage.getSize(),
                categoryPage.getTotalElements(),
                categoryPage.getTotalPages(),
                categoryPage.isLast()
        );
    }
}

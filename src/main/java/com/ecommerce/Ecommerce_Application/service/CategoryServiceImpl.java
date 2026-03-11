package com.ecommerce.Ecommerce_Application.service;

import com.ecommerce.Ecommerce_Application.exception.APIException;
import com.ecommerce.Ecommerce_Application.exception.ResourceNotFoundException;
import com.ecommerce.Ecommerce_Application.model.Category;
import com.ecommerce.Ecommerce_Application.payload.request.CategoryRequest;
import com.ecommerce.Ecommerce_Application.payload.response.CategoryResponse;
import com.ecommerce.Ecommerce_Application.payload.response.CategoryResponsePage;
import com.ecommerce.Ecommerce_Application.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponsePage getAllCategories(
            Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);
        List<Category> categories = categoryPage.getContent();

        if (categories.isEmpty())
            throw new APIException("No category created till now.");

        List<CategoryResponse> categoryDTOS = categories.stream().map(category ->
                        modelMapper.map(category, CategoryResponse.class)).toList();

        CategoryResponsePage categoryResponse = new CategoryResponsePage();
        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());
        return categoryResponse;
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest categoryDTO) {
        Category category = modelMapper.map(categoryDTO, Category.class);
        Category categoryFromDb = categoryRepository.findByCategoryName(category.getCategoryName());
        if (categoryFromDb != null)
            throw new APIException("Category with the name " + category.getCategoryName() + " already exists !!!");
        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryResponse.class);
    }

    @Override
    public CategoryResponse deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category","categoryId",categoryId));

        categoryRepository.delete(category);
        return modelMapper.map(category, CategoryResponse.class);
    }

    @Override
    public CategoryResponse updateCategory(CategoryRequest categoryDTO, Long categoryId) {
        Category savedCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category","categoryId",categoryId));
        savedCategory.setCategoryName(categoryDTO.getCategoryName());

        Category updatedCategory = categoryRepository.save(savedCategory);

        return modelMapper.map(updatedCategory, CategoryResponse.class);
    }
}

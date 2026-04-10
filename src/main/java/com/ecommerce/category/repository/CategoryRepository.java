package com.ecommerce.category.repository;

import com.ecommerce.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Category findByCategoryName(String categoryName);

    List<Category> findByParentIsNull();
}
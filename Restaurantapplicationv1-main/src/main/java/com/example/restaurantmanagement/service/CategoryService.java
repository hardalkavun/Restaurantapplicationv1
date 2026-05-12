package com.example.restaurantmanagement.service;

import com.example.restaurantmanagement.entity.Category;
import com.example.restaurantmanagement.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }

    public Category save(Category category) {
        category.setName(category.getName().trim());
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        Category category = findById(id);
        if (!category.getProducts().isEmpty()) {
            throw new IllegalStateException("Cannot delete a category that still has products");
        }
        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public long count() {
        return categoryRepository.count();
    }
}

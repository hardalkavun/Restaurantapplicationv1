package com.example.restaurantmanagement.service;

import com.example.restaurantmanagement.entity.Category;
import com.example.restaurantmanagement.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kategori iş kuralları: listeleme, kaydetme, silme öncesi ürün kontrolü.
 */
@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /** Tüm kategoriler (alfabetik sıra repository varsayılanına bağlı). */
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    /** ID ile getir; yoksa {@link EntityNotFoundException}. */
    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Kategori bulunamadı"));
    }

    /** İsim trimlenir ve kaydedilir (create/update). */
    public Category save(Category category) {
        category.setName(category.getName().trim());
        return categoryRepository.save(category);
    }

    /**
     * İçinde ürün varsa silinmez; iş kuralı ihlali {@link IllegalStateException}.
     */
    public void delete(Long id) {
        Category category = findById(id);
        if (!category.getProducts().isEmpty()) {
            throw new IllegalStateException("İçinde ürün bulunan kategori silinemez");
        }
        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public long count() {
        return categoryRepository.count();
    }
}

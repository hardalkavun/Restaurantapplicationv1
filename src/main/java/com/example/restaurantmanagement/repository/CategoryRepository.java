package com.example.restaurantmanagement.repository;

import com.example.restaurantmanagement.entity.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {@link Category} için veri erişim katmanı.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Başlangıç verisi ve tekrarlı ekleme önleme için büyük/küçük harf duyarsız isim arama. */
    Optional<Category> findByNameIgnoreCase(String name);
}

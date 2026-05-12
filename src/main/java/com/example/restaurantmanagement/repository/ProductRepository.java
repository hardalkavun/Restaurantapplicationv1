package com.example.restaurantmanagement.repository;

import com.example.restaurantmanagement.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * {@link Product} sorguları: arama, aktif filtre, kategori eager yükleme.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Başlangıç verisinde aynı isimli ürün var mı kontrolü (büyük/küçük harf duyarsız). */
    boolean existsByNameIgnoreCase(String name);

    /** Tüm ürünler; kategori tek sorguda çekilir (N+1 azaltma). */
    @EntityGraph(attributePaths = "category")
    List<Product> findAllByOrderByNameAsc();

    /**
     * Yönetici listesi: isim (LIKE) ve isteğe bağlı kategori filtresi.
     * {@code :name} / {@code :categoryId} null ise ilgili koşul devre dışı kalır.
     */
    @EntityGraph(attributePaths = "category")
    @Query("select p from Product p "
            + "where (:name is null or lower(p.name) like lower(concat('%', :name, '%'))) "
            + "and (:categoryId is null or p.category.id = :categoryId) "
            + "order by p.name asc")
    List<Product> search(@Param("name") String name, @Param("categoryId") Long categoryId);

    /** Halka açık menü: sadece active=true ürünler. */
    @EntityGraph(attributePaths = "category")
    @Query("select p from Product p "
            + "where p.active = true "
            + "and (:name is null or lower(p.name) like lower(concat('%', :name, '%'))) "
            + "and (:categoryId is null or p.category.id = :categoryId) "
            + "order by p.name asc")
    List<Product> searchActive(@Param("name") String name, @Param("categoryId") Long categoryId);

    /** Sipariş oluşturma ekranlarında aktif ürünler alfabetik. */
    List<Product> findByActiveTrueOrderByNameAsc();
}

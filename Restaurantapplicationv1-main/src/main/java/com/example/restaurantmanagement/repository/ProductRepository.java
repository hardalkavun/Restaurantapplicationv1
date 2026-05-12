package com.example.restaurantmanagement.repository;

import com.example.restaurantmanagement.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = "category")
    List<Product> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = "category")
    @Query("select p from Product p "
            + "where (:name is null or lower(p.name) like lower(concat('%', :name, '%'))) "
            + "and (:categoryId is null or p.category.id = :categoryId) "
            + "order by p.name asc")
    List<Product> search(@Param("name") String name, @Param("categoryId") Long categoryId);

    List<Product> findByActiveTrueOrderByNameAsc();
}

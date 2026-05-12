package com.example.restaurantmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * Ürün kategorisi (ör. Burgers, Drinks).
 * {@code categories} tablosu; isim benzersizdir.
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Kategori adı; doğrulama ve benzersiz kısıt için kullanılır. */
    @NotBlank(message = "Kategori adı zorunludur")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /** Bu kategoriye bağlı ürün listesi (ters yön ilişki). */
    @OneToMany(mappedBy = "category")
    private List<Product> products = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}

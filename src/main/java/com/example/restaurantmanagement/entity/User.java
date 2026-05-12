package com.example.restaurantmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Sistem kullanıcısı (yönetici, kurye veya müşteri).
 * {@code users} tablosuna eşlenir; şifre BCrypt ile saklanır.
 */
@Entity
@Table(name = "users")
public class User {

    /** Birincil anahtar; MySQL AUTO_INCREMENT. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Giriş kullanıcı adı; benzersiz. */
    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** BCrypt ile kodlanmış şifre (düz metin tutulmaz). */
    @NotBlank
    @Column(nullable = false)
    private String password;

    /** MANAGER, COURIER veya CUSTOMER. */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleEnum role;

    /**
     * Bu kullanıcıya atanmış siparişler (kurye tarafı).
     * İlişkinin sahibi {@link Order#assignedCourier} alanıdır (mappedBy).
     */
    @OneToMany(mappedBy = "assignedCourier")
    private List<Order> assignedOrders = new ArrayList<>();

    /** Müşteri hesabının verdiği siparişler. */
    @OneToMany(mappedBy = "customer")
    private List<Order> customerOrders = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }

    public List<Order> getAssignedOrders() {
        return assignedOrders;
    }

    public void setAssignedOrders(List<Order> assignedOrders) {
        this.assignedOrders = assignedOrders;
    }

    public List<Order> getCustomerOrders() {
        return customerOrders;
    }

    public void setCustomerOrders(List<Order> customerOrders) {
        this.customerOrders = customerOrders;
    }
}

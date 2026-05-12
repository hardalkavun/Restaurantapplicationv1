package com.example.restaurantmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Müşteri siparişi.
 * JPA entity adı {@code RestaurantOrder}: JPQL'de "Order" ile SQL çakışmasını önler.
 * Tablo: {@code orders}; ürünler {@code order_products} ara tablosu ile bağlanır.
 */
@Entity(name = "RestaurantOrder")
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Müşteri adı zorunludur")
    @Column(nullable = false, length = 120)
    private String customerName;

    @NotBlank(message = "Telefon zorunludur")
    @Column(nullable = false, length = 30)
    private String customerPhone;

    @NotBlank(message = "Adres zorunludur")
    @Column(nullable = false, length = 500)
    private String address;

    /** Siparişin oluşturulma veya kayda geçirilme zamanı. */
    @NotNull
    @Column(nullable = false)
    private LocalDateTime orderDateTime;

    /** {@link OrderStatus}; varsayılan hazırlanıyor. */
    @NotNull(message = "Durum zorunludur")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PREPARING;

    @Column(length = 1000)
    private String notes;

    /** Siparişi veren müşteri hesabı; yönetici tarafından açılan siparişlerde boş olabilir. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_user_id")
    private User customer;

    /** Atanan kurye (User, rolü COURIER olmalı); boş bırakılabilir. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_courier_id")
    private User assignedCourier;

    /**
     * Sipariş satırları: çoka çok ürün.
     * {@link JoinTable} ile {@code order_products} tablosu açıkça tanımlanır.
     */
    @NotEmpty(message = "En az bir ürün seçmelisiniz")
    @ManyToMany
    @JoinTable(
            name = "order_products",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getOrderDateTime() {
        return orderDateTime;
    }

    public void setOrderDateTime(LocalDateTime orderDateTime) {
        this.orderDateTime = orderDateTime;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public User getAssignedCourier() {
        return assignedCourier;
    }

    public void setAssignedCourier(User assignedCourier) {
        this.assignedCourier = assignedCourier;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    @Transient
    public BigDecimal getTotalAmount() {
        return products.stream()
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

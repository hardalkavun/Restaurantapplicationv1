package com.example.restaurantmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Menü ürünü.
 * Görsel bayt olarak {@code LONGBLOB} alanında tutulur; HTTP ile {@code /products/{id}/image} üzerinden sunulur.
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ürün adı zorunludur")
    @Column(nullable = false, length = 120)
    private String name;

    /** İsteğe bağlı açıklama metni. */
    @Column(length = 1000)
    private String description;

    /** Para birimi hassasiyeti için BigDecimal kullanılır. */
    @NotNull(message = "Fiyat zorunludur")
    @DecimalMin(value = "0.01", message = "Fiyat sıfırdan büyük olmalıdır")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** Menüde listelenip listelenmeyeceği. */
    @Column(nullable = false)
    private boolean active = true;

    /** Yüklenen dosyanın orijinal adı (temizlenmiş). */
    private String imageName;

    /** MIME türü (örn. image/png). */
    private String imageType;

    /** Ham görsel içeriği. */
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] imageData;

    /** Her ürünün tam olarak bir kategorisi vardır (FK: category_id). */
    @NotNull(message = "Kategori zorunludur")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** Siparişlerle çoka çok ilişkinin ters tarafı; sahip taraf Order.products. */
    @ManyToMany(mappedBy = "products")
    private List<Order> orders = new ArrayList<>();

    /** Görsel verisi var mı kontrolü (controller'da 404 için). */
    public boolean hasImage() {
        return imageData != null && imageData.length > 0;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}

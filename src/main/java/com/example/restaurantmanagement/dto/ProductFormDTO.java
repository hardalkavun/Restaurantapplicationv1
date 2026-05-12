package com.example.restaurantmanagement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import org.springframework.web.multipart.MultipartFile;

/**
 * Ürün oluşturma/düzenleme formunun taşıyıcısı.
 * Multipart dosya entity'ye bağlanmadığı için DTO kullanılır; servis {@code byte[]} kopyalar.
 */
public class ProductFormDTO {

    /** Düzenleme modunda dolu; yeni kayıtta null. */
    private Long id;

    @NotBlank(message = "Ürün adı zorunludur")
    private String name;

    private String description;

    @NotNull(message = "Fiyat zorunludur")
    @DecimalMin(value = "0.01", message = "Fiyat sıfırdan büyük olmalıdır")
    private BigDecimal price;

    /** Menüde gösterilsin mi. */
    private boolean active = true;

    /** Seçilen kategori PK'si. */
    @NotNull(message = "Kategori zorunludur")
    private Long categoryId;

    /** İsteğe bağlı yeni görsel yükleme; boşsa mevcut görsel korunur (güncellemede). */
    private MultipartFile imageFile;

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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public MultipartFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }
}

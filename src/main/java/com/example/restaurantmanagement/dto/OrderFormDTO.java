package com.example.restaurantmanagement.dto;

import com.example.restaurantmanagement.entity.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Sipariş formu (sepet): müşteri bilgileri, durum, kurye ve seçilen ürün ID listesi.
 * Controller {@code @ModelAttribute} ile bağlar; {@code @Valid} doğrulamayı tetikler.
 */
public class OrderFormDTO {

    private Long id;

    @NotBlank(message = "Müşteri adı zorunludur")
    private String customerName;

    @NotBlank(message = "Telefon zorunludur")
    private String customerPhone;

    @NotBlank(message = "Adres zorunludur")
    private String address;

    /** Yeni siparişte varsayılan; menü/checkout'ta da kullanılır. */
    @NotNull(message = "Durum zorunludur")
    private OrderStatus status = OrderStatus.PREPARING;

    private String notes;

    /** İsteğe bağlı; null ise kurye atanmaz. */
    private Long assignedCourierId;

    /** En az bir ürün seçilmeli (çoklu checkbox / sepet). */
    @NotEmpty(message = "En az bir ürün seçmelisiniz")
    private List<Long> productIds = new ArrayList<>();

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

    public Long getAssignedCourierId() {
        return assignedCourierId;
    }

    public void setAssignedCourierId(Long assignedCourierId) {
        this.assignedCourierId = assignedCourierId;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }
}

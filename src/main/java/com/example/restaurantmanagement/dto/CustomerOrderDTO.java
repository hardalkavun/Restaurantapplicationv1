package com.example.restaurantmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class CustomerOrderDTO {

    @NotBlank(message = "Ad soyad zorunludur")
    private String customerName;

    @NotBlank(message = "Telefon zorunludur")
    private String customerPhone;

    @NotBlank(message = "Adres zorunludur")
    private String address;

    private String notes;

    @NotEmpty(message = "En az bir ürün seçmelisiniz")
    private List<Long> productIds = new ArrayList<>();

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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }
}

package com.example.restaurantmanagement.entity;

/**
 * Sipariş yaşam döngüsü durumları.
 * Veritabanında string olarak saklanır (@Enumerated(STRING)).
 */
public enum OrderStatus {
    /** Müşteri siparişi alındı, restoran onayı bekliyor. */
    PENDING("Onay bekliyor"),
    /** Hazırlanıyor. */
    PREPARING("Hazırlanıyor"),
    /** Yolda. */
    ON_THE_WAY("Yolda"),
    /** Teslim edildi. */
    DELIVERED("Teslim edildi"),
    /** İptal. */
    CANCELLED("İptal edildi");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

package com.example.restaurantmanagement.entity;

/**
 * Uygulamadaki kullanıcı rolleri.
 * Spring Security yetkileri "ROLE_" + enum adı şeklinde üretilir (örn. ROLE_MANAGER).
 */
public enum RoleEnum {
    /** Yönetici: kategori, ürün, tüm sipariş işlemleri, panel. */
    MANAGER,
    /** Kurye: atanmış siparişleri görür, durum güncelleyebilir. */
    COURIER,
    /** Müşteri: menüden sipariş verir ve kendi siparişlerini takip eder. */
    CUSTOMER
}

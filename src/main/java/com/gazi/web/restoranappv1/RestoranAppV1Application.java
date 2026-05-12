package com.gazi.web.restoranappv1;

import com.example.restaurantmanagement.RestaurantManagementApplication;

/**
 * Alternatif main sınıfı (farklı paket adıyla çalıştırmak için).
 * Asıl Spring Boot başlatıcısı {@link RestaurantManagementApplication}'dır.
 */
public class RestoranAppV1Application {

    /** Uygulamayı {@link RestaurantManagementApplication} üzerinden başlatır. */
    public static void main(String[] args) {
        RestaurantManagementApplication.main(args);
    }
}

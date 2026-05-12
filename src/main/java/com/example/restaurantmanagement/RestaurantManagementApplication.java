package com.example.restaurantmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot uygulamasının giriş noktası.
 * {@link SpringBootApplication} bileşen taraması, otomatik yapılandırma ve gömülü web sunucusunu etkinleştirir.
 */
@SpringBootApplication
public class RestaurantManagementApplication {

    /**
     * JVM başlangıcı: Spring konteynerini ayağa kaldırır ve uygulamayı çalıştırır.
     *
     * @param args komut satırı argümanları (ör. --server.port)
     */
    public static void main(String[] args) {
        SpringApplication.run(RestaurantManagementApplication.class, args);
    }
}

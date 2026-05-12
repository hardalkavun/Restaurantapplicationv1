package com.example.restaurantmanagement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Basit proje sağlık testi: ana sınıf classpath'te yükleniyor mu. */
class RestaurantManagementApplicationTests {

    @Test
    void projectHasMainApplicationClass() {
        assertNotNull(RestaurantManagementApplication.class);
    }
}

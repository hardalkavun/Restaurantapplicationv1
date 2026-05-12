package com.example.restaurantmanagement.config;

import com.example.restaurantmanagement.entity.Category;
import com.example.restaurantmanagement.entity.Order;
import com.example.restaurantmanagement.entity.OrderStatus;
import com.example.restaurantmanagement.entity.Product;
import com.example.restaurantmanagement.entity.RoleEnum;
import com.example.restaurantmanagement.entity.User;
import com.example.restaurantmanagement.repository.CategoryRepository;
import com.example.restaurantmanagement.repository.OrderRepository;
import com.example.restaurantmanagement.repository.ProductRepository;
import com.example.restaurantmanagement.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final String SAMPLE_PNG_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=";

    @Bean
    CommandLineRunner initData(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            jdbcTemplate.execute("ALTER TABLE products MODIFY image_data LONGBLOB");

            User manager = createUserIfMissing(userRepository, passwordEncoder, "manager", RoleEnum.MANAGER);
            User courier = createUserIfMissing(userRepository, passwordEncoder, "courier", RoleEnum.COURIER);
            User customer = createUserIfMissing(userRepository, passwordEncoder, "customer", RoleEnum.CUSTOMER);

            Category mains = createCategoryIfMissing(categoryRepository, "Main Dishes");
            Category drinks = createCategoryIfMissing(categoryRepository, "Drinks");
            Category desserts = createCategoryIfMissing(categoryRepository, "Desserts");

            if (productRepository.count() == 0) {
                Product kebab = createProduct("Grilled Chicken Plate", "Chicken, rice, salad and house sauce",
                        new BigDecimal("180.00"), mains);
                Product ayran = createProduct("Ayran", "Cold yogurt drink",
                        new BigDecimal("35.00"), drinks);
                Product sutlac = createProduct("Rice Pudding", "Baked traditional rice pudding",
                        new BigDecimal("75.00"), desserts);
                productRepository.saveAll(List.of(kebab, ayran, sutlac));
            }

            if (orderRepository.count() == 0) {
                List<Product> products = productRepository.findByActiveTrueOrderByNameAsc();
                Order order = new Order();
                order.setCustomerName("Sample Customer");
                order.setCustomerPhone("0555 111 22 33");
                order.setAddress("Ataturk Street No: 10");
                order.setOrderDateTime(LocalDateTime.now());
                order.setStatus(OrderStatus.PENDING);
                order.setNotes("Please call before delivery.");
                order.setCustomer(customer);
                order.setAssignedCourier(courier);
                order.setProducts(new ArrayList<>(products.stream().limit(2).toList()));
                orderRepository.save(order);
            }

            // Ensures the manager variable is intentionally initialized in the seed flow.
            manager.getUsername();
        };
    }

    private User createUserIfMissing(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String username,
            RoleEnum role
    ) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode("1234"));
            user.setRole(role);
            return userRepository.save(user);
        });
    }

    private Category createCategoryIfMissing(CategoryRepository categoryRepository, String name) {
        return categoryRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Category category = new Category();
            category.setName(name);
            return categoryRepository.save(category);
        });
    }

    private Product createProduct(String name, String description, BigDecimal price, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setActive(true);
        product.setCategory(category);
        product.setImageName(name.toLowerCase().replace(' ', '-') + ".png");
        product.setImageType("image/png");
        product.setImageData(Base64.getDecoder().decode(SAMPLE_PNG_BASE64));
        return product;
    }
}

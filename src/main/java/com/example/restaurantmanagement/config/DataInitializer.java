// restaurantmanagement projesinin config paketinde bulunduğunu belirtir
package com.example.restaurantmanagement.config;

// Category entity sınıfını projeye dahil eder
import com.example.restaurantmanagement.entity.Category;

// Order entity sınıfını projeye dahil eder
import com.example.restaurantmanagement.entity.Order;

// Sipariş durum enumunu projeye dahil eder
import com.example.restaurantmanagement.entity.OrderStatus;

// Product entity sınıfını projeye dahil eder
import com.example.restaurantmanagement.entity.Product;

// Kullanıcı rol enumunu projeye dahil eder
import com.example.restaurantmanagement.entity.RoleEnum;

// User entity sınıfını projeye dahil eder
import com.example.restaurantmanagement.entity.User;

// Category veritabanı işlemleri için repository
import com.example.restaurantmanagement.repository.CategoryRepository;

// Order veritabanı işlemleri için repository
import com.example.restaurantmanagement.repository.OrderRepository;

// Product veritabanı işlemleri için repository
import com.example.restaurantmanagement.repository.ProductRepository;

// User veritabanı işlemleri için repository
import com.example.restaurantmanagement.repository.UserRepository;

// Para işlemlerinde hassas sayı kullanımı için BigDecimal sınıfı
import java.math.BigDecimal;

// Tarih ve saat işlemleri için LocalDateTime sınıfı
import java.time.LocalDateTime;

// Liste yapısı için List sınıfı
import java.util.List;

// UTF-8 karakter desteği için charset sınıfı
import java.nio.charset.StandardCharsets;

// Spring uygulama başlangıcında çalışacak yapı için CommandLineRunner
import org.springframework.boot.CommandLineRunner;

// Spring configuration anotasyonu
import org.springframework.context.annotation.Bean;

// Spring configuration anotasyonu
import org.springframework.context.annotation.Configuration;

// Şifre hashlemek için PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Uygulama ilk açılışında örnek kullanıcı, kategori, ürün ve demo sipariş oluşturur.
 * Spring ayağa kalktıktan sonra otomatik çalışır.
 */
@Configuration // Bu sınıfın bir Spring configuration sınıfı olduğunu belirtir
public class DataInitializer {

    /**
     * SVG şablonu.
     * %s kısmına dinamik içerik yerleştirilir.
     */
    private static final String SVG_WRAP =
            "<svg xmlns='http://www.w3.org/2000/svg' width='800' height='600' viewBox='0 0 800 600'>%s</svg>";

    /**
     * Spring uygulaması başlatıldığında çalışacak bean oluşturur.
     */
    @Bean
    CommandLineRunner initData(

            // Kullanıcı repository dependency injection
            UserRepository userRepository,

            // Kategori repository dependency injection
            CategoryRepository categoryRepository,

            // Ürün repository dependency injection
            ProductRepository productRepository,

            // Sipariş repository dependency injection
            OrderRepository orderRepository,

            // Şifre encoder dependency injection
            PasswordEncoder passwordEncoder

    ) {

        // Lambda expression ile uygulama başlangıcında çalışacak kodlar
        return args -> {

            // Manager kullanıcısı yoksa oluşturulur
            User manager = createUserIfMissing(
                    userRepository,
                    passwordEncoder,
                    "manager",
                    RoleEnum.MANAGER
            );

            // Courier kullanıcısı yoksa oluşturulur
            User courier = createUserIfMissing(
                    userRepository,
                    passwordEncoder,
                    "courier",
                    RoleEnum.COURIER
            );

            User customer = createUserIfMissing(
                    userRepository,
                    passwordEncoder,
                    "customer",
                    RoleEnum.CUSTOMER
            );

            // Main Dishes kategorisi yoksa oluşturulur
            Category mains = createCategoryIfMissing(
                    categoryRepository,
                    "Ana Yemekler"
            );

            // Burgers kategorisi yoksa oluşturulur
            Category burgers = createCategoryIfMissing(
                    categoryRepository,
                    "Burgerler"
            );

            // Pizzas kategorisi yoksa oluşturulur
            Category pizzas = createCategoryIfMissing(
                    categoryRepository,
                    "Pizzalar"
            );

            // Drinks kategorisi yoksa oluşturulur
            Category drinks = createCategoryIfMissing(
                    categoryRepository,
                    "İçecekler"
            );

            // Desserts kategorisi yoksa oluşturulur
            Category desserts = createCategoryIfMissing(
                    categoryRepository,
                    "Tatlılar"
            );

            // Grilled Chicken ürünü yoksa kaydedilir
            saveDemoProductIfMissing(
                    productRepository,
                    createSvgProduct(
                            "Izgara Tavuk Tabağı",
                            "Tavuk, pilav, salata ve özel sos",
                            new BigDecimal("180.00"),
                            mains,
                            svgFood("#F97316", "TAVUK")
                    )
            );

            // Iskender ürünü yoksa kaydedilir
            saveDemoProductIfMissing(
                    productRepository,
                    createSvgProduct(
                            "Iskender",
                            "Döner dilimleri, domates sosu, tereyağı ve yoğurt",
                            new BigDecimal("240.00"),
                            mains,
                            svgFood("#EF4444", "ISKENDER")
                    )
            );

            // Classic Burger ürünü yoksa kaydedilir
            saveDemoProductIfMissing(
                    productRepository,
                    createSvgProduct(
                            "Klasik Burger",
                            "Dana köfte, cheddar, turşu ve özel sos",
                            new BigDecimal("210.00"),
                            burgers,
                            svgFood("#F59E0B", "BURGER")
                    )
            );

            // Eğer hiç sipariş yoksa örnek sipariş oluşturulur
            if (orderRepository.count() == 0) {

                // Aktif ürünler alfabetik sırayla çekilir
                List<Product> products =
                        productRepository.findByActiveTrueOrderByNameAsc();

                // Yeni sipariş nesnesi oluşturulur
                Order order = new Order();

                // Müşteri adı atanır
                order.setCustomer(customer);

                order.setCustomerName("Müşteri");

                // Müşteri telefonu atanır
                order.setCustomerPhone("0555 111 22 33");

                // Adres atanır
                order.setAddress("Atatürk Caddesi No: 10");

                // Sipariş tarihi atanır
                order.setOrderDateTime(LocalDateTime.now());

                // Sipariş durumu hazırlanıyor olarak atanır
                order.setStatus(OrderStatus.PREPARING);

                // Sipariş notu atanır
                order.setNotes("Teslimattan önce arayınız.");

                // Kurye atanır
                order.setAssignedCourier(courier);

                // İlk iki ürün siparişe eklenir
                order.setProducts(
                        products.stream().limit(2).toList()
                );

                // Sipariş veritabanına kaydedilir
                orderRepository.save(order);
            }

            // manager nesnesinin kullanılması sağlanır
            manager.getUsername();
        };
    }

    /**
     * Kullanıcı varsa getirir, yoksa oluşturur.
     */
    private User createUserIfMissing(

            // User repository parametresi
            UserRepository userRepository,

            // Şifre encoder parametresi
            PasswordEncoder passwordEncoder,

            // Kullanıcı adı parametresi
            String username,

            // Rol parametresi
            RoleEnum role

    ) {

        // Kullanıcı var mı kontrol edilir
        return userRepository.findByUsername(username)

                // Kullanıcı yoksa yeni kullanıcı oluşturulur
                .orElseGet(() -> {

                    // Yeni User nesnesi oluşturulur
                    User user = new User();

                    // Kullanıcı adı atanır
                    user.setUsername(username);

                    // Şifre encode edilerek atanır
                    user.setPassword(passwordEncoder.encode("1234"));

                    // Rol atanır
                    user.setRole(role);

                    // Kullanıcı veritabanına kaydedilir
                    return userRepository.save(user);
                });
    }

    /**
     * Kategori varsa getirir, yoksa oluşturur.
     */
    private Category createCategoryIfMissing(

            // Category repository parametresi
            CategoryRepository categoryRepository,

            // Kategori adı parametresi
            String name

    ) {

        // Aynı isimde kategori aranır
        return categoryRepository.findByNameIgnoreCase(name)

                // Bulunamazsa yeni kategori oluşturulur
                .orElseGet(() -> {

                    // Yeni Category nesnesi oluşturulur
                    Category category = new Category();

                    // Kategori adı atanır
                    category.setName(name);

                    // Veritabanına kaydedilir
                    return categoryRepository.save(category);
                });
    }

    /**
     * Ürün aynı isimde yoksa kaydeder.
     */
    private void saveDemoProductIfMissing(

            // Product repository parametresi
            ProductRepository productRepository,

            // Kaydedilecek ürün
            Product candidate

    ) {

        // Aynı isimde ürün var mı kontrol edilir
        if (!productRepository.existsByNameIgnoreCase(candidate.getName())) {

            // Ürün yoksa kaydedilir
            productRepository.save(candidate);
        }
    }

    /**
     * SVG görselli ürün nesnesi oluşturur.
     */
    private Product createSvgProduct(

            // Ürün adı
            String name,

            // Ürün açıklaması
            String description,

            // Ürün fiyatı
            BigDecimal price,

            // Ürün kategorisi
            Category category,

            // SVG görsel içeriği
            String svg

    ) {

        // Yeni Product nesnesi oluşturulur
        Product product = new Product();

        // Ürün adı atanır
        product.setName(name);

        // Ürün açıklaması atanır
        product.setDescription(description);

        // Fiyat atanır
        product.setPrice(price);

        // Ürün aktif yapılır
        product.setActive(true);

        // Kategori atanır
        product.setCategory(category);

        // Görsel dosya adı oluşturulur
        product.setImageName(
                name.toLowerCase().replace(' ', '-') + ".svg"
        );

        // Görsel mime type atanır
        product.setImageType("image/svg+xml");

        // SVG UTF-8 byte dizisine çevrilerek kaydedilir
        product.setImageData(
                svg.getBytes(StandardCharsets.UTF_8)
        );

        // Product nesnesi döndürülür
        return product;
    }

    /**
     * Yemek kartı SVG tasarımı oluşturur.
     */
    private String svgFood(String color, String label) {

        // SVG içeriği oluşturulur
        String inner = String.join(
                "\n",

                "<defs>",
                "  <linearGradient id='g' x1='0' y1='0' x2='1' y2='1'>",
                "    <stop offset='0' stop-color='%s' stop-opacity='0.95'/>",
                "    <stop offset='1' stop-color='#ffffff' stop-opacity='0.12'/>",
                "  </linearGradient>",
                "</defs>",

                "<rect width='800' height='600' fill='url(#g)'/>",

                "<circle cx='620' cy='170' r='120' fill='#ffffff' opacity='0.18'/>",

                "<circle cx='170' cy='420' r='160' fill='#ffffff' opacity='0.12'/>",

                "<rect x='90' y='120' width='620' height='360' rx='32' fill='#ffffff' opacity='0.22'/>",

                "<text x='120' y='260' font-size='54' font-family='Segoe UI, Arial' fill='#111827' font-weight='800'>%s</text>"
        );

        // SVG wrap içine yerleştirilip döndürülür
        return String.format(
                SVG_WRAP,
                String.format(inner, color, label)
        );
    }
}

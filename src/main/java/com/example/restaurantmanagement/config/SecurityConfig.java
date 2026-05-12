// restaurantmanagement projesinin config paketinde bulunduğunu belirtir
package com.example.restaurantmanagement.config;

// Custom UserDetailsService sınıfını projeye dahil eder
import com.example.restaurantmanagement.security.CustomUserDetailsService;

// Spring Bean tanımlamak için gerekli anotasyon
import org.springframework.context.annotation.Bean;

// Spring configuration sınıfı olduğunu belirtir
import org.springframework.context.annotation.Configuration;

// Veritabanı tabanlı authentication provider sınıfı
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

// Metot seviyesinde güvenlik (@PreAuthorize vb.) desteğini aktif eder
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// HTTP güvenlik ayarları için gerekli sınıf
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

// Spring Security web güvenliğini aktif eder
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

// BCrypt şifreleme algoritması
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// Şifre encoder interface'i
import org.springframework.security.crypto.password.PasswordEncoder;

// Başarılı login sonrası işlem yapmak için handler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

// Security filter zinciri sınıfı
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security yapılandırma sınıfı.
 * URL yetkilendirme, login/logout, şifreleme ve rol bazlı erişim ayarlarını içerir.
 */
@Configuration // Spring configuration sınıfı olduğunu belirtir
@EnableWebSecurity // Web security özelliğini aktif eder
@EnableMethodSecurity // @PreAuthorize gibi anotasyonları aktif eder
public class SecurityConfig {

    // Kullanıcı doğrulama işlemleri için özel service
    private final CustomUserDetailsService userDetailsService;

    /**
     * Constructor injection ile CustomUserDetailsService alınır.
     */
    public SecurityConfig(CustomUserDetailsService userDetailsService) {

        // Gelen service değişkene atanır
        this.userDetailsService = userDetailsService;
    }

    /**
     * HTTP güvenlik ayarlarını yapılandırır.
     */
    @Bean // Spring bean olarak kaydedilir
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // HTTP güvenlik ayarları başlatılır
        http

                // URL bazlı yetkilendirme ayarları
                .authorizeHttpRequests(auth -> auth

                        // Login sayfası ve statik dosyalar herkese açık
                        .requestMatchers(
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // Ana sayfa ve menü herkese açık
                        .requestMatchers(
                                "/",
                                "/menu/**"
                        ).permitAll()

                        // Ürün görselleri herkese açık
                        .requestMatchers(
                                "/products/*/image"
                        ).permitAll()

                        // Sadece yönetici erişebilir
                        .requestMatchers(
                                "/products/**",
                                "/categories/**",
                                "/dashboard"
                        ).hasRole("MANAGER")

                        // Müşteri ekranları yalnızca müşteri rolüne açık
                        .requestMatchers(
                                "/customer/**"
                        ).hasRole("CUSTOMER")

                        // Sipariş oluşturma ve düzenleme işlemleri sadece yönetici
                        .requestMatchers(
                                "/orders/new",
                                "/orders/*/edit",
                                "/orders/*/delete",
                                "/orders/create-cart"
                        ).hasRole("MANAGER")

                        // Courier sayfaları sadece courier rolüne açık
                        .requestMatchers(
                                "/courier/**"
                        ).hasRole("COURIER")

                        // Yönetici ve kurye sipariş yönetimi
                        .requestMatchers(
                                "/orders/**"
                        ).hasAnyRole("MANAGER", "COURIER")

                        // Diğer tüm isteklerde giriş zorunlu
                        .anyRequest().authenticated()
                )

                // Form login ayarları
                .formLogin(form -> form

                        // Özel login sayfası yolu
                        .loginPage("/login")

                        // Login işleminin yapılacağı endpoint
                        .loginProcessingUrl("/login")

                        // Başarılı login sonrası yönlendirme işlemi
                        .successHandler(
                                roleBasedAuthenticationSuccessHandler()
                        )

                        // Başarısız login sonrası hata parametresi
                        .failureUrl("/login?error=true")

                        // Login sayfasına herkes erişebilir
                        .permitAll()
                )

                // Logout ayarları
                .logout(logout -> logout

                        // Logout endpoint
                        .logoutUrl("/logout")

                        // Başarılı logout sonrası yönlendirme
                        .logoutSuccessUrl("/login?logout=true")

                        // Session temizlenir
                        .invalidateHttpSession(true)

                        // JSESSIONID cookie silinir
                        .deleteCookies("JSESSIONID")

                        // Logout işlemi herkese açık
                        .permitAll()
                )

                // Yetkisiz erişim durumunda yönlendirilecek sayfa
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-denied")
                )

                // Session yönetimi ayarları
                .sessionManagement(session -> session

                        // Aynı kullanıcı için maksimum 1 oturum
                        .maximumSessions(1)

                        // Yeni girişte eski oturum kapatılır
                        .maxSessionsPreventsLogin(false)
                )

                // Authentication provider eklenir
                .authenticationProvider(authenticationProvider());

        // Oluşturulan security filter chain döndürülür
        return http.build();
    }

    /**
     * Veritabanı tabanlı authentication provider oluşturur.
     */
    @Bean // Spring bean olarak kaydedilir
    public DaoAuthenticationProvider authenticationProvider() {

        // DaoAuthenticationProvider nesnesi oluşturulur
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        // Şifre encoder atanır
        provider.setPasswordEncoder(passwordEncoder());

        // Provider döndürülür
        return provider;
    }

    /**
     * BCrypt tabanlı password encoder oluşturur.
     */
    @Bean // Spring bean olarak kaydedilir
    public PasswordEncoder passwordEncoder() {

        // BCryptPasswordEncoder nesnesi döndürülür
        return new BCryptPasswordEncoder();
    }

    /**
     * Başarılı login sonrası rol bazlı yönlendirme yapar.
     */
    @Bean // Spring bean olarak kaydedilir
    public AuthenticationSuccessHandler roleBasedAuthenticationSuccessHandler() {

        // Lambda ile AuthenticationSuccessHandler oluşturulur
        return (request, response, authentication) -> {

            boolean manager = authentication.getAuthorities()

                    // Roller stream'e çevrilir
                    .stream()

                    // ROLE_MANAGER kontrolü yapılır
                    .anyMatch(authority ->
                            authority.getAuthority().equals("ROLE_MANAGER")
                    );

            boolean customer = authentication.getAuthorities()
                    .stream()
                    .anyMatch(authority ->
                            authority.getAuthority().equals("ROLE_CUSTOMER")
                    );

            if (manager) {
                response.sendRedirect("/dashboard");
            } else if (customer) {
                response.sendRedirect("/customer/menu");
            } else {
                response.sendRedirect("/courier/orders");
            }
        };
    }
}

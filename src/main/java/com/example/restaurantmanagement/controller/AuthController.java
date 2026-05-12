// restaurantmanagement projesinin controller paketinde bulunduğunu belirtir
package com.example.restaurantmanagement.controller;

// Spring Security Authentication sınıfını projeye dahil eder
import org.springframework.security.core.Authentication;

// Bu sınıfın bir Spring MVC Controller olduğunu belirtir
import org.springframework.stereotype.Controller;

// View tarafına veri göndermek için kullanılan Model sınıfı
import org.springframework.ui.Model;

// GET requestlerini yakalamak için kullanılan anotasyon
import org.springframework.web.bind.annotation.GetMapping;

// URL query parametrelerini almak için kullanılan anotasyon
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Giriş işlemleri, ana yönlendirme ve erişim reddi sayfalarını yöneten controller.
 */
@Controller // Bu sınıfın bir Spring MVC controller olduğunu belirtir
public class AuthController {

    /**
     * Ana sayfa isteğini yönetir.
     * Kullanıcı giriş yapmamışsa menüye,
     * manager ise dashboard'a,
     * courier ise orders sayfasına yönlendirir.
     */
    @GetMapping("/") // "/" endpointine gelen GET isteğini karşılar
    public String home(Authentication authentication) {

        // Kullanıcı giriş yapmamışsa veya authentication null ise
        if (authentication == null || !authentication.isAuthenticated()) {

            return "redirect:/login";
        }

        // Kullanıcının ROLE_MANAGER yetkisine sahip olup olmadığı kontrol edilir
        boolean manager = authentication.getAuthorities()

                // Roller stream yapısına çevrilir
                .stream()

                // ROLE_MANAGER rolü var mı kontrol edilir
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_MANAGER")
                );

        boolean customer = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_CUSTOMER")
                );

        if (manager) {
            return "redirect:/dashboard";
        }
        if (customer) {
            return "redirect:/customer/menu";
        }
        return "redirect:/courier/orders";
    }

    /**
     * Login sayfasını görüntüler.
     * Hatalı giriş veya logout mesajlarını modele ekler.
     */
    @GetMapping("/login") // "/login" endpointine gelen GET isteğini karşılar
    public String login(

            // URL'deki error parametresi alınır
            @RequestParam(
                    value = "error",
                    required = false
            ) String error,

            // URL'deki logout parametresi alınır
            @RequestParam(
                    value = "logout",
                    required = false
            ) String logout,

            // View tarafına veri göndermek için model nesnesi
            Model model
    ) {

        // Eğer login sırasında hata oluşmuşsa
        if (error != null) {

            // Hata mesajı modele eklenir
            model.addAttribute(
                    "errorMessage",
                    "Kullanıcı adı veya şifre hatalı."
            );
        }

        // Eğer kullanıcı logout olmuşsa
        if (logout != null) {

            // Başarılı çıkış mesajı modele eklenir
            model.addAttribute(
                    "successMessage",
                    "Başarıyla çıkış yapıldı."
            );
        }

        // login.html sayfası döndürülür
        return "login";
    }

    /**
     * Yetkisiz erişim durumunda gösterilecek sayfa.
     */
    @GetMapping("/access-denied") // "/access-denied" endpointini karşılar
    public String accessDenied() {

        // access-denied.html sayfasını döndürür
        return "access-denied";
    }
}

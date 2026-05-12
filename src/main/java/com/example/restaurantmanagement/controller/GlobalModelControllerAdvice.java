package com.example.restaurantmanagement.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Tüm {@code @Controller} yanıtlarına ortak model öznitelikleri ekler (layout menüsü vb.).
 */
@ControllerAdvice
public class GlobalModelControllerAdvice {

    /** Üst barda gösterilecek oturum kullanıcı adı; anonim ise null. */
    @ModelAttribute("navUsername")
    public String navUsername(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }

    /** Menü linklerini yöneticiye göre göstermek için. */
    @ModelAttribute("navIsManager")
    public boolean navIsManager(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_MANAGER"));
    }

    @ModelAttribute("navIsCourier")
    public boolean navIsCourier(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_COURIER"));
    }

    @ModelAttribute("navIsCustomer")
    public boolean navIsCustomer(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CUSTOMER"));
    }

    /** Aktif URL ile vurgulu navigasyon için (şablonlarda kullanılabilir). */
    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request == null ? "" : request.getRequestURI();
    }
}

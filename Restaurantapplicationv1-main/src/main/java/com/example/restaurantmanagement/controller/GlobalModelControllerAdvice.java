package com.example.restaurantmanagement.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelControllerAdvice {

    @ModelAttribute("navUsername")
    public String navUsername(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }

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
}

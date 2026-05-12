package com.example.restaurantmanagement.controller;

import com.example.restaurantmanagement.entity.OrderStatus;
import com.example.restaurantmanagement.service.CategoryService;
import com.example.restaurantmanagement.service.OrderService;
import com.example.restaurantmanagement.service.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Yönetici özet paneli: varlık sayıları ve durum bazlı sipariş özetleri.
 */
@Controller
public class DashboardController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;

    public DashboardController(ProductService productService, CategoryService categoryService, OrderService orderService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
    }

    /** Thymeleaf {@code dashboard.html} için sayım model öznitelikleri. */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('MANAGER')")
    public String dashboard(Model model) {
        model.addAttribute("productCount", productService.count());
        model.addAttribute("categoryCount", categoryService.count());
        model.addAttribute("orderCount", orderService.count());
        model.addAttribute("preparingCount", orderService.countByStatus(OrderStatus.PREPARING));
        model.addAttribute("onTheWayCount", orderService.countByStatus(OrderStatus.ON_THE_WAY));
        return "dashboard";
    }
}

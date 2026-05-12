package com.example.restaurantmanagement.controller;

import com.example.restaurantmanagement.entity.OrderStatus;
import com.example.restaurantmanagement.service.CategoryService;
import com.example.restaurantmanagement.service.OrderService;
import com.example.restaurantmanagement.service.ProductService;
import com.example.restaurantmanagement.service.UserService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserService userService;

    public DashboardController(
            ProductService productService,
            CategoryService categoryService,
            OrderService orderService,
            UserService userService
    ) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @ModelAttribute("statuses")
    public OrderStatus[] statuses() {
        return OrderStatus.values();
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('MANAGER')")
    public String dashboard(
            @RequestParam(value = "customerName", required = false) String customerName,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "status", required = false) OrderStatus status,
            Model model
    ) {
        model.addAttribute("productCount", productService.count());
        model.addAttribute("categoryCount", categoryService.count());
        model.addAttribute("orderCount", orderService.count());
        model.addAttribute("preparingCount", orderService.countByStatus(OrderStatus.PREPARING));
        model.addAttribute("onTheWayCount", orderService.countByStatus(OrderStatus.ON_THE_WAY));
        model.addAttribute("pendingCount", orderService.countByStatus(OrderStatus.PENDING));
        model.addAttribute("orders", orderService.search(customerName, date, status));
        model.addAttribute("couriers", userService.findCouriers());
        model.addAttribute("customerName", customerName);
        model.addAttribute("date", date);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("isManager", true);
        return "dashboard";
    }
}

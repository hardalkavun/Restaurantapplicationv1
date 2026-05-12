package com.example.restaurantmanagement.controller;

import com.example.restaurantmanagement.entity.User;
import com.example.restaurantmanagement.service.OrderService;
import com.example.restaurantmanagement.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Kurye oturumu: yalnızca kendisine atanmış siparişleri listeler.
 */
@Controller
@RequestMapping("/courier")
@PreAuthorize("hasRole('COURIER')")
public class CourierController {

    private final OrderService orderService;
    private final UserService userService;

    public CourierController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    /** Principal kullanıcı adından {@link User} bulunur, siparişler kuryeye göre filtrelenir. */
    @GetMapping("/orders")
    public String myOrders(Model model, Authentication authentication) {
        User courier = userService.findByUsername(authentication.getName());
        model.addAttribute("orders", orderService.findByAssignedCourier(courier));
        return "courier/orders";
    }
}

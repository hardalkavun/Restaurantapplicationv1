package com.example.restaurantmanagement.controller;

import com.example.restaurantmanagement.entity.Order;
import com.example.restaurantmanagement.entity.User;
import com.example.restaurantmanagement.service.OrderService;
import com.example.restaurantmanagement.service.UserService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customers")
@PreAuthorize("hasRole('MANAGER')")
public class CustomerManagementController {

    private final UserService userService;
    private final OrderService orderService;

    public CustomerManagementController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping
    public String list(Model model) {
        List<User> customers = userService.findCustomers();
        Map<Long, List<Order>> ordersByCustomer = new LinkedHashMap<>();
        for (User customer : customers) {
            ordersByCustomer.put(customer.getId(), orderService.findForCustomer(customer.getUsername()));
        }
        model.addAttribute("customers", customers);
        model.addAttribute("ordersByCustomer", ordersByCustomer);
        return "customers/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        User customer = userService.findById(id);
        model.addAttribute("customer", customer);
        model.addAttribute("orders", orderService.findForCustomer(customer.getUsername()));
        return "customers/detail";
    }
}

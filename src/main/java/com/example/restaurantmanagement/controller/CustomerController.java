package com.example.restaurantmanagement.controller;

import com.example.restaurantmanagement.dto.CustomerOrderDTO;
import com.example.restaurantmanagement.entity.Order;
import com.example.restaurantmanagement.service.OrderService;
import com.example.restaurantmanagement.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {

    private final ProductService productService;
    private final OrderService orderService;

    public CustomerController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    @GetMapping("/menu")
    public String menu(Model model, Authentication authentication) {
        CustomerOrderDTO orderForm = new CustomerOrderDTO();
        orderForm.setCustomerName(authentication.getName());
        model.addAttribute("orderForm", orderForm);
        model.addAttribute("products", productService.findActiveProducts());
        return "customer/menu";
    }

    @PostMapping("/orders")
    public String placeOrder(
            @Valid @ModelAttribute("orderForm") CustomerOrderDTO orderForm,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("products", productService.findActiveProducts());
            return "customer/menu";
        }
        Order order = orderService.createCustomerOrder(orderForm, authentication.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Siparişiniz alındı. Durumunu bu ekrandan takip edebilirsiniz.");
        return "redirect:/customer/orders/" + order.getId();
    }

    @GetMapping("/orders")
    public String myOrders(Authentication authentication, Model model) {
        model.addAttribute("orders", orderService.findForCustomer(authentication.getName()));
        return "customer/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Authentication authentication, Model model) {
        Order order = orderService.findById(id);
        if (!orderService.belongsToCustomer(order, authentication.getName())) {
            throw new AccessDeniedException("Sadece kendi siparişinizi görüntüleyebilirsiniz.");
        }
        model.addAttribute("order", order);
        return "customer/order-detail";
    }
}

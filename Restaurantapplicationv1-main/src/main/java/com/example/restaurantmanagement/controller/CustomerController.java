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
    public String menu(Model model) {
        model.addAttribute("products", productService.findActiveProducts());
        return "customer/menu";
    }

    @GetMapping("/orders/new")
    public String orderForm(Model model, Authentication authentication) {
        CustomerOrderDTO orderForm = new CustomerOrderDTO();
        orderForm.setCustomerName(authentication.getName());
        model.addAttribute("orderForm", orderForm);
        model.addAttribute("pageTitle", "Place Order");
        model.addAttribute("formAction", "/customer/orders");
        model.addAttribute("products", productService.findActiveProducts());
        return "customer/order-form";
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
            model.addAttribute("pageTitle", "Place Order");
            model.addAttribute("formAction", "/customer/orders");
            model.addAttribute("products", productService.findActiveProducts());
            return "customer/order-form";
        }
        Order order = orderService.createCustomerOrder(orderForm, authentication.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Order placed. A manager will approve it soon.");
        return "redirect:/customer/orders/" + order.getId();
    }

    @GetMapping("/orders/{id}/edit")
    public String editOrderForm(@PathVariable Long id, Authentication authentication, Model model) {
        Order order = orderService.findById(id);
        validateCustomerCanEdit(order, authentication.getName());
        model.addAttribute("orderForm", orderService.toCustomerForm(order));
        model.addAttribute("order", order);
        model.addAttribute("pageTitle", "Edit Order");
        model.addAttribute("formAction", "/customer/orders/" + id);
        model.addAttribute("products", productService.findActiveProducts());
        return "customer/order-form";
    }

    @PostMapping("/orders/{id}")
    public String updateOrder(
            @PathVariable Long id,
            @Valid @ModelAttribute("orderForm") CustomerOrderDTO orderForm,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Order order = orderService.findById(id);
        validateCustomerCanEdit(order, authentication.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("order", order);
            model.addAttribute("pageTitle", "Edit Order");
            model.addAttribute("formAction", "/customer/orders/" + id);
            model.addAttribute("products", productService.findActiveProducts());
            return "customer/order-form";
        }
        orderService.updateCustomerOrder(id, orderForm, authentication.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Order updated successfully.");
        return "redirect:/customer/orders/" + id;
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
            throw new AccessDeniedException("You can view only your own orders.");
        }
        model.addAttribute("order", order);
        model.addAttribute("canEdit", orderService.isCustomerEditable(order));
        return "customer/order-detail";
    }

    private void validateCustomerCanEdit(Order order, String username) {
        if (!orderService.belongsToCustomer(order, username)) {
            throw new AccessDeniedException("You can edit only your own orders.");
        }
        if (!orderService.isCustomerEditable(order)) {
            throw new AccessDeniedException("This order can no longer be edited.");
        }
    }
}

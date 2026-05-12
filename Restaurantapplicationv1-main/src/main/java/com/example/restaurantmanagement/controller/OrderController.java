package com.example.restaurantmanagement.controller;

import com.example.restaurantmanagement.dto.OrderFormDTO;
import com.example.restaurantmanagement.entity.Order;
import com.example.restaurantmanagement.entity.OrderStatus;
import com.example.restaurantmanagement.service.OrderService;
import com.example.restaurantmanagement.service.ProductService;
import com.example.restaurantmanagement.service.UserService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;

    public OrderController(OrderService orderService, ProductService productService, UserService userService) {
        this.orderService = orderService;
        this.productService = productService;
        this.userService = userService;
    }

    @ModelAttribute("statuses")
    public OrderStatus[] statuses() {
        return OrderStatus.values();
    }

    @ModelAttribute("courierStatuses")
    public OrderStatus[] courierStatuses() {
        return new OrderStatus[] {OrderStatus.ON_THE_WAY, OrderStatus.DELIVERED};
    }

    @GetMapping
    public String list(
            @RequestParam(value = "customerName", required = false) String customerName,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "status", required = false) OrderStatus status,
            Authentication authentication,
            Model model
    ) {
        if (isManager(authentication)) {
            model.addAttribute("orders", orderService.search(customerName, date, status));
            model.addAttribute("couriers", userService.findCouriers());
        } else {
            model.addAttribute("orders", orderService.findForCourier(authentication.getName()));
        }
        model.addAttribute("customerName", customerName);
        model.addAttribute("date", date);
        model.addAttribute("selectedStatus", status);
        addSecurityModel(model, authentication);
        return "orders/list";
    }

    @GetMapping("/new")
    @PreAuthorize("denyAll()")
    public String createForm(Model model) {
        model.addAttribute("orderForm", new OrderFormDTO());
        addFormReferenceData(model, "New Order");
        return "orders/form";
    }

    @PostMapping
    @PreAuthorize("denyAll()")
    public String create(
            @Valid @ModelAttribute("orderForm") OrderFormDTO orderForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addFormReferenceData(model, "New Order");
            return "orders/form";
        }
        Order order = orderService.create(orderForm);
        redirectAttributes.addFlashAttribute("successMessage", "Order created successfully.");
        return "redirect:/orders/" + order.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Authentication authentication, Model model) {
        Order order = orderService.findById(id);
        if (!isManager(authentication) && !orderService.isAssignedTo(order, authentication.getName())) {
            throw new AccessDeniedException("Couriers can view only assigned orders.");
        }
        model.addAttribute("order", order);
        if (isManager(authentication) && order.getCustomer() != null) {
            model.addAttribute("customerHistory", orderService.findForCustomer(order.getCustomer().getUsername()));
        }
        addSecurityModel(model, authentication);
        return "orders/detail";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('MANAGER')")
    public String editForm(@PathVariable Long id, Model model) {
        Order order = orderService.findById(id);
        model.addAttribute("orderForm", orderService.toForm(order));
        addFormReferenceData(model, "Edit Order");
        return "orders/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("orderForm") OrderFormDTO orderForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            orderForm.setId(id);
            addFormReferenceData(model, "Edit Order");
            return "orders/form";
        }
        orderService.update(id, orderForm);
        redirectAttributes.addFlashAttribute("successMessage", "Order updated successfully.");
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            orderService.updateStatus(id, status, authentication.getName(), isManager(authentication));
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated successfully.");
        } catch (AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('MANAGER')")
    public String assignCourier(
            @PathVariable Long id,
            @RequestParam(required = false) Long courierId,
            @RequestParam(required = false) String returnTo,
            RedirectAttributes redirectAttributes
    ) {
        try {
            orderService.assignCourier(id, courierId);
            redirectAttributes.addFlashAttribute("successMessage", "Courier assignment updated.");
        } catch (AccessDeniedException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return redirectAfterStaffAction(returnTo);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public String approve(
            @PathVariable Long id,
            @RequestParam(required = false) Long courierId,
            @RequestParam(required = false) String returnTo,
            RedirectAttributes redirectAttributes
    ) {
        try {
            orderService.approve(id, courierId);
            redirectAttributes.addFlashAttribute("successMessage", "Order approved.");
        } catch (AccessDeniedException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return redirectAfterStaffAction(returnTo);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('MANAGER')")
    public String cancel(
            @PathVariable Long id,
            @RequestParam(required = false) String returnTo,
            RedirectAttributes redirectAttributes
    ) {
        try {
            orderService.cancel(id);
            redirectAttributes.addFlashAttribute("successMessage", "Order cancelled.");
        } catch (AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return redirectAfterStaffAction(returnTo);
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('MANAGER')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        orderService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Order deleted successfully.");
        return "redirect:/orders";
    }

    private void addFormReferenceData(Model model, String pageTitle) {
        model.addAttribute("products", productService.findActiveProducts());
        model.addAttribute("couriers", userService.findCouriers());
        model.addAttribute("pageTitle", pageTitle);
    }

    private void addSecurityModel(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("isManager", isManager(authentication));
        model.addAttribute("isCourier", !isManager(authentication));
    }

    private boolean isManager(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_MANAGER"));
    }

    private String redirectAfterStaffAction(String returnTo) {
        return "dashboard".equals(returnTo) ? "redirect:/dashboard" : "redirect:/orders";
    }
}

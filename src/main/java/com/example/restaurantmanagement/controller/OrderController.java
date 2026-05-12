package com.example.restaurantmanagement.controller;

import com.example.restaurantmanagement.dto.OrderFormDTO;
import com.example.restaurantmanagement.entity.Order;
import com.example.restaurantmanagement.entity.OrderStatus;
import com.example.restaurantmanagement.service.OrderService;
import com.example.restaurantmanagement.service.ProductService;
import com.example.restaurantmanagement.service.UserService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
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

/**
 * Sipariş listesi, detay, oluşturma (sepet), düzenleme, durum güncelleme ve silme.
 * Rol kuralları metot başına {@code @PreAuthorize} veya servis içi {@link AccessDeniedException} ile desteklenir.
 */
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

    /** Tüm sipariş formlarında durum açılır listesi için sabit model. */
    @ModelAttribute("statuses")
    public OrderStatus[] statuses() {
        return OrderStatus.values();
    }

    /** Yönetici: sepetli yeni sipariş ekranı (ürün + kurye + boş form). */
    @GetMapping("/create-cart")
    @PreAuthorize("hasRole('MANAGER')")
    public String createCartPage(Model model) {
        model.addAttribute("products", productService.findActiveProducts());
        model.addAttribute("couriers", userService.findCouriers());
        model.addAttribute("orderForm", new OrderFormDTO());
        model.addAttribute("pageTitle", "Yeni Sipariş");
        return "orders/create-cart";
    }

    /**
     * Filtreli liste; kurye de tüm listeyi görebilir (detayda/serviste kısıt uygulanabilir).
     * Şablonda kullanıcı adı ve yönetici bayrağı için model doldurulur.
     */
    @GetMapping
    public String list(
            @RequestParam(value = "customerName", required = false) String customerName,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "status", required = false) OrderStatus status,
            Authentication authentication,
            Model model
    ) {
        model.addAttribute("orders", orderService.search(customerName, date, status));
        model.addAttribute("customerName", customerName);
        model.addAttribute("date", date);
        model.addAttribute("selectedStatus", status);
        addSecurityModel(model, authentication);
        return "orders/list";
    }

    /** Eski {@code /new} URL'i sepet sayfasına yönlendirir (aynı şablon). */
    @GetMapping("/new")
    @PreAuthorize("hasRole('MANAGER')")
    public String createForm(Model model) {
        model.addAttribute("products", productService.findActiveProducts());
        model.addAttribute("couriers", userService.findCouriers());
        model.addAttribute("orderForm", new OrderFormDTO());
        model.addAttribute("pageTitle", "Yeni Sipariş");
        return "orders/create-cart";
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public String create(
            @Valid @ModelAttribute("orderForm") OrderFormDTO orderForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addFormReferenceData(model, "Yeni Sipariş");
            return "orders/create-cart";
        }
        Order order = orderService.create(orderForm);
        redirectAttributes.addFlashAttribute("successMessage", "Sipariş oluşturuldu.");
        return "redirect:/orders/" + order.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Authentication authentication, Model model) {
        Order order = orderService.findById(id);
        model.addAttribute("order", order);
        addSecurityModel(model, authentication);
        return "orders/detail";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('MANAGER')")
    public String editForm(@PathVariable Long id, Model model) {
        Order order = orderService.findById(id);
        model.addAttribute("orderForm", orderService.toForm(order));
        addFormReferenceData(model, "Siparişi Düzenle");
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
            addFormReferenceData(model, "Siparişi Düzenle");
            return "orders/form";
        }
        orderService.update(id, orderForm);
        redirectAttributes.addFlashAttribute("successMessage", "Sipariş güncellendi.");
        return "redirect:/orders/" + id;
    }

    /**
     * Liste üzerinden durum değişimi; kurye sadece atanmış siparişte (servis kontrolü).
     */
    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            orderService.updateStatus(id, status, authentication.getName(), isManager(authentication));
            redirectAttributes.addFlashAttribute("successMessage", "Sipariş durumu güncellendi.");
        } catch (AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('MANAGER')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        orderService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Sipariş silindi.");
        return "redirect:/orders";
    }

    /** Sipariş formları için ürün, kurye listesi ve başlık. */
    private void addFormReferenceData(Model model, String pageTitle) {
        model.addAttribute("products", productService.findActiveProducts());
        model.addAttribute("couriers", userService.findCouriers());
        model.addAttribute("pageTitle", pageTitle);
    }

    /** Şablonda kullanıcı adı ve yönetici mi bilgisi. */
    private void addSecurityModel(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        boolean manager = isManager(authentication);
        model.addAttribute("isManager", manager);
        model.addAttribute("statusOptions", manager
                ? List.of(OrderStatus.PREPARING, OrderStatus.CANCELLED)
                : List.of(OrderStatus.ON_THE_WAY, OrderStatus.DELIVERED));
    }

    private boolean isManager(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_MANAGER"));
    }
}

package com.example.restaurantmanagement.controller;

import com.example.restaurantmanagement.dto.OrderFormDTO;
import com.example.restaurantmanagement.service.CategoryService;
import com.example.restaurantmanagement.service.OrderService;
import com.example.restaurantmanagement.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Halka açık menü (GET) ve misafir sipariş oluşturma (POST checkout).
 * Güvenlikte {@code /menu/**} permitAll; sipariş yine servis üzerinden veritabanına yazılır.
 */
@Controller
@RequestMapping("/menu")
public class MenuController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;

    public MenuController(ProductService productService, CategoryService categoryService, OrderService orderService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
    }

    /** Arama ve kategori filtresi; yan tarafta sepet formu için boş {@link OrderFormDTO}. */
    @GetMapping
    public String menu(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            Model model
    ) {
        model.addAttribute("pageTitle", "Menü");
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("products", productService.searchActive(q, categoryId));
        model.addAttribute("orderForm", new OrderFormDTO());
        return "menu/index";
    }

    /**
     * Menüden sipariş: doğrulama başarısızsa menüyü hata ile yeniden doldurur.
     * Başarıda flash mesaj ile menüye redirect (PRG pattern).
     */
    @PostMapping("/checkout")
    public String checkout(
            @Valid @ModelAttribute("orderForm") OrderFormDTO orderForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Menü");
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("products", productService.searchActive(null, null));
            model.addAttribute("errorMessage", "Lütfen müşteri bilgilerini doldurun ve en az bir ürün ekleyin.");
            return "menu/index";
        }

        var order = orderService.create(orderForm);
        redirectAttributes.addFlashAttribute("successMessage",
                "Siparişiniz alındı. Sipariş numaranız #" + order.getId() + ".");
        return "redirect:/menu";
    }
}

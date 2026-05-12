package com.example.restaurantmanagement.controller;

import com.example.restaurantmanagement.entity.Category;
import com.example.restaurantmanagement.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Kategori CRUD; tüm metotlar yönetici rolü gerektirir ({@code @PreAuthorize} sınıf düzeyinde).
 */
@Controller
@RequestMapping("/categories")
@PreAuthorize("hasRole('MANAGER')")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "categories/list";
    }

    /** Boş entity ile yeni kategori formu. */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("pageTitle", "Yeni Kategori");
        return "categories/form";
    }

    /** POST create: doğrulama hatasında formu tekrar gösterir. */
    @PostMapping
    public String create(
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Yeni Kategori");
            return "categories/form";
        }
        categoryService.save(category);
        redirectAttributes.addFlashAttribute("successMessage", "Kategori oluşturuldu.");
        return "redirect:/categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.findById(id));
        model.addAttribute("pageTitle", "Kategoriyi Düzenle");
        return "categories/form";
    }

    /** POST update: path id ile form id hizalanır. */
    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            category.setId(id);
            model.addAttribute("pageTitle", "Kategoriyi Düzenle");
            return "categories/form";
        }
        category.setId(id);
        categoryService.save(category);
        redirectAttributes.addFlashAttribute("successMessage", "Kategori güncellendi.");
        return "redirect:/categories";
    }

    /** İçinde ürün varsa servis istisnası yakalanır ve flash hata mesajı verilir. */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Kategori silindi.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/categories";
    }
}

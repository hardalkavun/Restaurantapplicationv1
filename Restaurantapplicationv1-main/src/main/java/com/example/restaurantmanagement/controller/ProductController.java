package com.example.restaurantmanagement.controller;

import com.example.restaurantmanagement.dto.ProductFormDTO;
import com.example.restaurantmanagement.entity.Product;
import com.example.restaurantmanagement.service.CategoryService;
import com.example.restaurantmanagement.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.dao.DataAccessException;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public String list(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            Model model
    ) {
        model.addAttribute("products", productService.search(name, categoryId));
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("name", name);
        model.addAttribute("categoryId", categoryId);
        return "products/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('MANAGER')")
    public String createForm(Model model) {
        model.addAttribute("productForm", new ProductFormDTO());
        addFormReferenceData(model, "New Product");
        return "products/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public String create(
            @Valid @ModelAttribute("productForm") ProductFormDTO productForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addFormReferenceData(model, "New Product");
            return "products/form";
        }
        try {
            productService.create(productForm);
        } catch (IllegalArgumentException | IllegalStateException | DataAccessException ex) {
            model.addAttribute("errorMessage", productSaveErrorMessage(ex));
            addFormReferenceData(model, "New Product");
            return "products/form";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Product created successfully.");
        return "redirect:/products";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('MANAGER')")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("productForm", productService.toForm(product));
        model.addAttribute("product", product);
        addFormReferenceData(model, "Edit Product");
        return "products/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("productForm") ProductFormDTO productForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            productForm.setId(id);
            model.addAttribute("product", productService.findById(id));
            addFormReferenceData(model, "Edit Product");
            return "products/form";
        }
        try {
            productService.update(id, productForm);
        } catch (IllegalArgumentException | IllegalStateException | DataAccessException ex) {
            productForm.setId(id);
            model.addAttribute("product", productService.findById(id));
            model.addAttribute("errorMessage", productSaveErrorMessage(ex));
            addFormReferenceData(model, "Edit Product");
            return "products/form";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully.");
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('MANAGER')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product could not be deleted because it may be used in an order.");
        }
        return "redirect:/products";
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable Long id) {
        Product product = productService.findById(id);
        if (!product.hasImage()) {
            return ResponseEntity.notFound().build();
        }
        String contentType = product.getImageType() == null || product.getImageType().isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : product.getImageType();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .cacheControl(CacheControl.noCache())
                .body(product.getImageData());
    }

    private void addFormReferenceData(Model model, String pageTitle) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("pageTitle", pageTitle);
    }

    private String productSaveErrorMessage(Exception ex) {
        if (ex instanceof DataAccessException) {
            return "Product could not be saved. If you uploaded an image, make sure the product image column is LONGBLOB in MySQL.";
        }
        return ex.getMessage() == null ? "Product could not be saved." : ex.getMessage();
    }
}

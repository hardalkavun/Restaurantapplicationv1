package com.example.restaurantmanagement.service;

import com.example.restaurantmanagement.dto.ProductFormDTO;
import com.example.restaurantmanagement.entity.Category;
import com.example.restaurantmanagement.entity.Product;
import com.example.restaurantmanagement.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Ürün CRUD, arama ve form DTO ile entity eşleme (görsel yükleme dahil).
 */
@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    /** Yönetici listesi: isim + isteğe bağlı kategori filtresi (tüm ürünler). */
    @Transactional(readOnly = true)
    public List<Product> search(String name, Long categoryId) {
        return productRepository.search(blankToNull(name), categoryId);
    }

    /** Halka açık menü araması: sadece aktif ürünler. */
    @Transactional(readOnly = true)
    public List<Product> searchActive(String name, Long categoryId) {
        return productRepository.searchActive(blankToNull(name), categoryId);
    }

    /** Sipariş formlarında seçilebilir ürün listesi. */
    @Transactional(readOnly = true)
    public List<Product> findActiveProducts() {
        return productRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ürün bulunamadı"));
    }

    /** Yeni ürün: boş entity + form alanları + isteğe bağlı dosya. */
    public Product create(ProductFormDTO form) {
        Product product = new Product();
        applyForm(product, form);
        return productRepository.save(product);
    }

    /** Mevcut ürünü bul, formdan güncelle; yeni dosya yoksa görsel alanları dokunulmaz kalabilir. */
    public Product update(Long id, ProductFormDTO form) {
        Product product = findById(id);
        applyForm(product, form);
        return productRepository.save(product);
    }

    public void delete(Long id) {
        productRepository.delete(findById(id));
    }

    /** Düzenleme formunu doldurmak için entity → DTO (dosya geri yüklenmez). */
    @Transactional(readOnly = true)
    public ProductFormDTO toForm(Product product) {
        ProductFormDTO form = new ProductFormDTO();
        form.setId(product.getId());
        form.setName(product.getName());
        form.setDescription(product.getDescription());
        form.setPrice(product.getPrice());
        form.setActive(product.isActive());
        form.setCategoryId(product.getCategory().getId());
        return form;
    }

    @Transactional(readOnly = true)
    public long count() {
        return productRepository.count();
    }

    /** Ortak alan atama: kategori çözümleme, trim, görsel kopyalama. */
    private void applyForm(Product product, ProductFormDTO form) {
        Category category = categoryService.findById(form.getCategoryId());
        product.setName(form.getName().trim());
        product.setDescription(blankToNull(form.getDescription()));
        product.setPrice(form.getPrice());
        product.setActive(form.isActive());
        product.setCategory(category);
        copyImageIfPresent(product, form.getImageFile());
    }

    /** Yüklenen dosya varsa ad, MIME ve baytları entity'ye yazar. */
    private void copyImageIfPresent(Product product, MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return;
        }
        try {
            product.setImageName(StringUtils.cleanPath(imageFile.getOriginalFilename()));
            product.setImageType(imageFile.getContentType());
            product.setImageData(imageFile.getBytes());
        } catch (IOException ex) {
            throw new IllegalStateException("Yüklenen görsel okunamadı", ex);
        }
    }

    /** Boş veya sadece boşluk string → null (JPQL "is null" dalları için). */
    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

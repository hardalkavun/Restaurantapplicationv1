package com.example.restaurantmanagement.service;

import com.example.restaurantmanagement.dto.ProductFormDTO;
import com.example.restaurantmanagement.entity.Category;
import com.example.restaurantmanagement.entity.Product;
import com.example.restaurantmanagement.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class ProductService {

    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public List<Product> search(String name, Long categoryId) {
        return productRepository.search(blankToNull(name), categoryId);
    }

    @Transactional(readOnly = true)
    public List<Product> findActiveProducts() {
        return productRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }

    public Product create(ProductFormDTO form) {
        Product product = new Product();
        applyForm(product, form);
        return productRepository.save(product);
    }

    public Product update(Long id, ProductFormDTO form) {
        Product product = findById(id);
        applyForm(product, form);
        return productRepository.save(product);
    }

    public void delete(Long id) {
        productRepository.delete(findById(id));
    }

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

    private void applyForm(Product product, ProductFormDTO form) {
        Category category = categoryService.findById(form.getCategoryId());
        product.setName(form.getName().trim());
        product.setDescription(blankToNull(form.getDescription()));
        product.setPrice(form.getPrice());
        product.setActive(form.isActive());
        product.setCategory(category);
        copyImageIfPresent(product, form.getImageFile());
    }

    private void copyImageIfPresent(Product product, MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return;
        }
        if (imageFile.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException("Image size must be 5 MB or smaller.");
        }
        String contentType = imageFile.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files can be uploaded.");
        }
        try {
            String originalFilename = imageFile.getOriginalFilename();
            product.setImageName(StringUtils.hasText(originalFilename)
                    ? StringUtils.cleanPath(originalFilename)
                    : "product-image");
            product.setImageType(StringUtils.hasText(contentType) ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE);
            product.setImageData(imageFile.getBytes());
        } catch (IOException ex) {
            throw new IllegalStateException("Could not read uploaded image", ex);
        }
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

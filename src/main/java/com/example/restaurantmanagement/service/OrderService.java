package com.example.restaurantmanagement.service;

import com.example.restaurantmanagement.dto.CustomerOrderDTO;
import com.example.restaurantmanagement.dto.OrderFormDTO;
import com.example.restaurantmanagement.entity.Order;
import com.example.restaurantmanagement.entity.OrderStatus;
import com.example.restaurantmanagement.entity.Product;
import com.example.restaurantmanagement.entity.RoleEnum;
import com.example.restaurantmanagement.entity.User;
import com.example.restaurantmanagement.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Sipariş oluşturma/güncelleme, arama, durum değişikliği ve kurye atama kuralları.
 */
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final UserService userService;

    public OrderService(OrderRepository orderRepository, ProductService productService, UserService userService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.userService = userService;
    }

    /**
     * Liste ekranı filtreleri: müşteri adı parça eşleşme, durum, tek gün (start/end hesaplanır).
     */
    @Transactional(readOnly = true)
    public List<Order> search(String customerName, LocalDate date, OrderStatus status) {
        LocalDateTime start = date == null ? null : date.atStartOfDay();
        LocalDateTime end = date == null ? null : date.plusDays(1).atStartOfDay();
        String normalizedName = StringUtils.hasText(customerName) ? customerName.trim() : null;
        return orderRepository.search(normalizedName, status, start, end);
    }

    /** Kurye paneli: sadece bu kullanıcıya atanmış siparişler. */
    @Transactional(readOnly = true)
    public List<Order> findByAssignedCourier(User courier) {
        return orderRepository.findByAssignedCourierOrderByOrderDateTimeDesc(courier);
    }

    /** Müşteri paneli: sadece oturumdaki müşterinin siparişleri. */
    @Transactional(readOnly = true)
    public List<Order> findForCustomer(String username) {
        return orderRepository.findByCustomerUsernameOrderByOrderDateTimeDesc(username);
    }

    /** Detay: ürünler ve ilişkiler graph ile yüklenmiş olmalı. */
    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findWithProductsById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sipariş bulunamadı"));
    }

    /** Yeni sipariş: zaman damgası sunucu saati, ürünler formdan çözülür. */
    public Order create(OrderFormDTO form) {
        Order order = new Order();
        order.setOrderDateTime(LocalDateTime.now());
        applyForm(order, form);
        return orderRepository.save(order);
    }

    /** Müşteri siparişi: kullanıcıya bağlanır, başlangıç durumu onay bekliyor olur. */
    public Order createCustomerOrder(CustomerOrderDTO form, String username) {
        User customer = userService.findByUsername(username);
        if (customer.getRole() != RoleEnum.CUSTOMER) {
            throw new AccessDeniedException("Sadece müşteri hesabı sipariş verebilir.");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setCustomerName(form.getCustomerName().trim());
        order.setCustomerPhone(form.getCustomerPhone().trim());
        order.setAddress(form.getAddress().trim());
        order.setOrderDateTime(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setNotes(StringUtils.hasText(form.getNotes()) ? form.getNotes().trim() : null);
        order.setProducts(new ArrayList<>(form.getProductIds().stream().map(productService::findById).toList()));
        return orderRepository.save(order);
    }

    public Order update(Long id, OrderFormDTO form) {
        Order order = findById(id);
        applyForm(order, form);
        return orderRepository.save(order);
    }

    /** Yönetici onay/iptal eder; kurye yalnızca teslimat durumlarını günceller. */
    public void updateStatus(Long id, OrderStatus status, String username, boolean manager) {
        Order order = findById(id);
        if (manager) {
            updateManagerStatus(order, status);
        } else {
            updateCourierStatus(order, status, username);
        }
        orderRepository.save(order);
    }

    public void delete(Long id) {
        orderRepository.delete(findById(id));
    }

    /** Düzenleme formu: entity → DTO (ürünler ID listesine dönüştürülür). */
    @Transactional(readOnly = true)
    public OrderFormDTO toForm(Order order) {
        OrderFormDTO form = new OrderFormDTO();
        form.setId(order.getId());
        form.setCustomerName(order.getCustomerName());
        form.setCustomerPhone(order.getCustomerPhone());
        form.setAddress(order.getAddress());
        form.setStatus(order.getStatus());
        form.setNotes(order.getNotes());
        if (order.getAssignedCourier() != null) {
            form.setAssignedCourierId(order.getAssignedCourier().getId());
        }
        form.setProductIds(order.getProducts().stream().map(Product::getId).toList());
        return form;
    }

    @Transactional(readOnly = true)
    public long count() {
        return orderRepository.count();
    }

    @Transactional(readOnly = true)
    public long countByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    /** Kurye kullanıcı adı, siparişteki atanmış kurye ile eşleşiyor mu. */
    public boolean isAssignedTo(Order order, String username) {
        return order.getAssignedCourier() != null
                && order.getAssignedCourier().getUsername().equals(username);
    }

    public boolean belongsToCustomer(Order order, String username) {
        return order.getCustomer() != null && order.getCustomer().getUsername().equals(username);
    }

    /** Form alanlarını entity'ye yazar; ürün ve kurye FK'ları çözülür. */
    private void applyForm(Order order, OrderFormDTO form) {
        order.setCustomerName(form.getCustomerName().trim());
        order.setCustomerPhone(form.getCustomerPhone().trim());
        order.setAddress(form.getAddress().trim());
        order.setStatus(form.getStatus());
        order.setNotes(StringUtils.hasText(form.getNotes()) ? form.getNotes().trim() : null);
        order.setAssignedCourier(resolveCourier(form.getAssignedCourierId()));
        order.setProducts(new ArrayList<>(form.getProductIds().stream().map(productService::findById).toList()));
    }

    private void updateManagerStatus(Order order, OrderStatus status) {
        if (status != OrderStatus.PREPARING && status != OrderStatus.CANCELLED) {
            throw new AccessDeniedException("Yönetici siparişi yalnızca hazırlanmaya alabilir veya iptal edebilir.");
        }
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new AccessDeniedException("Teslim edilmiş veya iptal edilmiş sipariş değiştirilemez.");
        }
        order.setStatus(status);
    }

    private void updateCourierStatus(Order order, OrderStatus status, String username) {
        if (!isAssignedTo(order, username)) {
            throw new AccessDeniedException("Kurye sadece kendisine atanmış siparişleri güncelleyebilir.");
        }
        if (status != OrderStatus.ON_THE_WAY && status != OrderStatus.DELIVERED) {
            throw new AccessDeniedException("Kurye siparişi yalnızca yolda veya teslim edildi yapabilir.");
        }
        if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CANCELLED) {
            throw new AccessDeniedException("Bu sipariş kurye durum güncellemesine hazır değil.");
        }
        order.setStatus(status);
    }

    /** ID verilmişse kullanıcı bulunur ve rolünün COURIER olması zorunludur. */
    private User resolveCourier(Long courierId) {
        if (courierId == null) {
            return null;
        }
        User courier = userService.findById(courierId);
        if (courier.getRole() != RoleEnum.COURIER) {
            throw new IllegalArgumentException("Atanan kullanıcı kurye olmalıdır");
        }
        return courier;
    }
}

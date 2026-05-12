package com.example.restaurantmanagement.service;

import com.example.restaurantmanagement.dto.OrderFormDTO;
import com.example.restaurantmanagement.dto.CustomerOrderDTO;
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

    @Transactional(readOnly = true)
    public List<Order> search(String customerName, LocalDate date, OrderStatus status) {
        LocalDateTime start = date == null ? null : date.atStartOfDay();
        LocalDateTime end = date == null ? null : date.plusDays(1).atStartOfDay();
        String normalizedName = StringUtils.hasText(customerName) ? customerName.trim() : null;
        return orderRepository.search(normalizedName, status, start, end);
    }

    @Transactional(readOnly = true)
    public List<Order> findForCourier(String username) {
        return orderRepository.findByAssignedCourierUsernameOrderByOrderDateTimeDesc(username);
    }

    @Transactional(readOnly = true)
    public List<Order> findForCustomer(String username) {
        return orderRepository.findByCustomerUsernameOrderByOrderDateTimeDesc(username);
    }

    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findWithProductsById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    public Order create(OrderFormDTO form) {
        Order order = new Order();
        order.setOrderDateTime(LocalDateTime.now());
        applyForm(order, form);
        return orderRepository.save(order);
    }

    public Order createCustomerOrder(CustomerOrderDTO form, String username) {
        User customer = userService.findByUsername(username);
        if (customer.getRole() != RoleEnum.CUSTOMER) {
            throw new AccessDeniedException("Only customers can place menu orders");
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

    public Order updateCustomerOrder(Long id, CustomerOrderDTO form, String username) {
        Order order = findById(id);
        if (!belongsToCustomer(order, username)) {
            throw new AccessDeniedException("You can edit only your own orders.");
        }
        if (!isCustomerEditable(order)) {
            throw new AccessDeniedException("This order can no longer be edited because delivery has already started or the order is closed.");
        }
        order.setCustomerName(form.getCustomerName().trim());
        order.setCustomerPhone(form.getCustomerPhone().trim());
        order.setAddress(form.getAddress().trim());
        order.setNotes(StringUtils.hasText(form.getNotes()) ? form.getNotes().trim() : null);
        order.setProducts(new ArrayList<>(form.getProductIds().stream().map(productService::findById).toList()));
        return orderRepository.save(order);
    }

    public Order update(Long id, OrderFormDTO form) {
        Order order = findById(id);
        applyForm(order, form);
        return orderRepository.save(order);
    }

    public void updateStatus(Long id, OrderStatus status, String username, boolean manager) {
        Order order = findById(id);
        if (manager) {
            updateManagerStatus(order, status);
        } else {
            updateCourierStatus(order, status, username);
        }
        orderRepository.save(order);
    }

    public void assignCourier(Long id, Long courierId) {
        Order order = findById(id);
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new AccessDeniedException("Courier cannot be assigned to completed or cancelled orders.");
        }
        order.setAssignedCourier(resolveCourier(courierId));
        orderRepository.save(order);
    }

    public void approve(Long id, Long courierId) {
        Order order = findById(id);
        if (courierId != null) {
            order.setAssignedCourier(resolveCourier(courierId));
        }
        updateManagerStatus(order, OrderStatus.PREPARING);
        orderRepository.save(order);
    }

    public void cancel(Long id) {
        Order order = findById(id);
        updateManagerStatus(order, OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public void delete(Long id) {
        orderRepository.delete(findById(id));
    }

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
        form.setProductIds(new ArrayList<>(order.getProducts().stream().map(Product::getId).toList()));
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

    public boolean isAssignedTo(Order order, String username) {
        return order.getAssignedCourier() != null
                && order.getAssignedCourier().getUsername().equals(username);
    }

    public boolean belongsToCustomer(Order order, String username) {
        return order.getCustomer() != null && order.getCustomer().getUsername().equals(username);
    }

    public boolean isCustomerEditable(Order order) {
        return order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PREPARING;
    }

    @Transactional(readOnly = true)
    public CustomerOrderDTO toCustomerForm(Order order) {
        CustomerOrderDTO form = new CustomerOrderDTO();
        form.setCustomerName(order.getCustomerName());
        form.setCustomerPhone(order.getCustomerPhone());
        form.setAddress(order.getAddress());
        form.setNotes(order.getNotes());
        form.setProductIds(new ArrayList<>(order.getProducts().stream().map(Product::getId).toList()));
        return form;
    }

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
            throw new AccessDeniedException("Managers can only approve orders as preparing or cancel orders.");
        }
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new AccessDeniedException("Completed or cancelled orders cannot be changed by manager.");
        }
        order.setStatus(status);
    }

    private void updateCourierStatus(Order order, OrderStatus status, String username) {
        if (!isAssignedTo(order, username)) {
            throw new AccessDeniedException("Couriers can update only their assigned orders.");
        }
        if (status != OrderStatus.ON_THE_WAY && status != OrderStatus.DELIVERED) {
            throw new AccessDeniedException("Couriers can only mark orders as on the way or delivered.");
        }
        if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CANCELLED) {
            throw new AccessDeniedException("This order is not ready for courier status updates.");
        }
        order.setStatus(status);
    }

    private User resolveCourier(Long courierId) {
        if (courierId == null) {
            return null;
        }
        User courier = userService.findById(courierId);
        if (courier.getRole() != RoleEnum.COURIER) {
            throw new IllegalArgumentException("Assigned user must be a courier");
        }
        return courier;
    }
}

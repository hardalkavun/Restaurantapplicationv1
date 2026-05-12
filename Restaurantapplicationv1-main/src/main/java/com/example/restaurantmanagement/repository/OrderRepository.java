package com.example.restaurantmanagement.repository;

import com.example.restaurantmanagement.entity.Order;
import com.example.restaurantmanagement.entity.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"customer", "assignedCourier", "products", "products.category"})
    @Query("select distinct o from RestaurantOrder o "
            + "where (:customerName is null or lower(o.customerName) like lower(concat('%', :customerName, '%'))) "
            + "and (:status is null or o.status = :status) "
            + "and (:start is null or o.orderDateTime >= :start) "
            + "and (:end is null or o.orderDateTime < :end) "
            + "order by o.orderDateTime desc")
    List<Order> search(
            @Param("customerName") String customerName,
            @Param("status") OrderStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @EntityGraph(attributePaths = {"customer", "assignedCourier", "products", "products.category"})
    Optional<Order> findWithProductsById(Long id);

    @EntityGraph(attributePaths = {"customer", "assignedCourier", "products", "products.category"})
    List<Order> findByAssignedCourierUsernameOrderByOrderDateTimeDesc(String username);

    @EntityGraph(attributePaths = {"customer", "assignedCourier", "products", "products.category"})
    List<Order> findByCustomerUsernameOrderByOrderDateTimeDesc(String username);

    long countByStatus(OrderStatus status);
}

package com.example.restaurantmanagement.repository;

import com.example.restaurantmanagement.entity.Order;
import com.example.restaurantmanagement.entity.OrderStatus;
import com.example.restaurantmanagement.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * {@link Order} (JPA adı: RestaurantOrder) için özel arama ve detay sorguları.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Filtreli sipariş listesi: müşteri adı, durum, tarih aralığı.
     * {@code end} gün sonundan önce (exclusive) kullanılır: aynı günün tüm saatleri dahil.
     */
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

    /** Detay sayfası: kurye + ürünler + ürün kategorileri tek seferde. */
    @EntityGraph(attributePaths = {"customer", "assignedCourier", "products", "products.category"})
    Optional<Order> findWithProductsById(Long id);

    /** Dashboard için durum başına adet. */
    long countByStatus(OrderStatus status);

    /** Kurye paneli: bu kullanıcıya atanmış siparişler, yeniden eskiye. */
    @EntityGraph(attributePaths = {"customer", "assignedCourier", "products", "products.category"})
    List<Order> findByAssignedCourierOrderByOrderDateTimeDesc(User courier);

    /** Müşteri paneli: bu kullanıcıya ait siparişler, yeniden eskiye. */
    @EntityGraph(attributePaths = {"customer", "assignedCourier", "products", "products.category"})
    List<Order> findByCustomerUsernameOrderByOrderDateTimeDesc(String username);
}

package com.example.restaurantmanagement.repository;

import com.example.restaurantmanagement.entity.RoleEnum;
import com.example.restaurantmanagement.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {@link User} varlığı için Spring Data JPA deposu.
 * Temel CRUD {@link JpaRepository} üzerinden gelir; aşağıdaki metotlar isimlendirme sözleşmesiyle üretilir.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Spring Security giriş akışında kullanıcıyı kullanıcı adına göre bulur. */
    Optional<User> findByUsername(String username);

    /** Kayıt / doğrulama senaryolarında kullanıcı adı var mı kontrolü. */
    boolean existsByUsername(String username);

    /** Sipariş formunda kurye seçim listesi için sadece COURIER rolündekiler. */
    List<User> findByRole(RoleEnum role);
}

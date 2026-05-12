package com.example.restaurantmanagement.repository;

import com.example.restaurantmanagement.entity.RoleEnum;
import com.example.restaurantmanagement.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByRole(RoleEnum role);
}

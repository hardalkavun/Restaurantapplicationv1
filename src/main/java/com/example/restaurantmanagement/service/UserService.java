package com.example.restaurantmanagement.service;

import com.example.restaurantmanagement.entity.RoleEnum;
import com.example.restaurantmanagement.entity.User;
import com.example.restaurantmanagement.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kullanıcı okuma işlemleri (sipariş formu kurye listesi, kurye profil çözümleme).
 * Şifre değiştirme bu projede yoktur.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Spring Security principal adı (username) ile kullanıcı bulma. */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı"));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı"));
    }

    /** Sipariş oluşturma/düzenlemede dropdown: sadece kurye rolü. */
    public List<User> findCouriers() {
        return userRepository.findByRole(RoleEnum.COURIER);
    }

    /** Yönetici raporları veya müşteri listeleri için müşteri hesapları. */
    public List<User> findCustomers() {
        return userRepository.findByRole(RoleEnum.CUSTOMER);
    }
}

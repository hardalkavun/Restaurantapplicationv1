package com.example.restaurantmanagement.service;

import com.example.restaurantmanagement.dto.RegisterDTO;
import com.example.restaurantmanagement.entity.RoleEnum;
import com.example.restaurantmanagement.entity.User;
import com.example.restaurantmanagement.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<User> findCouriers() {
        return userRepository.findByRole(RoleEnum.COURIER);
    }

    @Transactional(readOnly = true)
    public List<User> findCustomers() {
        return userRepository.findByRole(RoleEnum.CUSTOMER);
    }

    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public User register(RegisterDTO registerDTO) {
        String username = registerDTO.getUsername().trim();
        if (usernameExists(username)) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRole(registerDTO.getRole());
        return userRepository.save(user);
    }
}

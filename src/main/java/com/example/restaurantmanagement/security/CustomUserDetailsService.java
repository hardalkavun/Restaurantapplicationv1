package com.example.restaurantmanagement.security;

import com.example.restaurantmanagement.entity.User;
import com.example.restaurantmanagement.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security form login: kullanıcı adıyla veritabanından {@link User} yükler
 * ve {@link UserDetails} nesnesine dönüştürür (şifre hash karşılaştırması burada değil, encoder'da).
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Başarısız girişte genel mesaj için aynı istisna tipi kullanılır (kullanıcı enumeration azaltılır).
     * Yetki adı Spring kuralı: "ROLE_" + enum (örn. ROLE_MANAGER).
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı adı veya şifre hatalı"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                .build();
    }
}

package com.speakit.speakit.service.user;

import com.speakit.speakit.model.user.User;
import com.speakit.speakit.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * CustomUserDetailsService는 Spring Security의 UserDetailsService 인터페이스를 구현합니다.
 * - 역할: 인증 과정에서 사용자의 이메일(혹은 username)을 기반으로 DB에서 사용자 정보를 조회하여,
 *         Spring Security가 사용할 수 있는 UserDetails 객체로 변환합니다.
 * - 이 클래스는 인증(Authentication) 과정에만 관여하며, 주로 AuthenticationManager에 의해 호출됩니다.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if(user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}

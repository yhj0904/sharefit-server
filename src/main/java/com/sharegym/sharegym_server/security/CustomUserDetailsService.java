package com.sharegym.sharegym_server.security;

import com.sharegym.sharegym_server.entity.User;
import com.sharegym.sharegym_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security UserDetailsService 구현체
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 이메일로 사용자 조회
     * Spring Security에서 username 파라미터 이름을 사용하지만, 실제로는 이메일을 받음
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 이메일로만 조회 (일관성 개선)
        User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found with email: " + email)
            );

        return UserPrincipal.create(user);
    }

    /**
     * ID로 사용자 조회 (추가 메서드)
     * 필요한 경우 ID로도 조회 가능하도록 별도 메서드 제공
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found with id: " + userId)
            );

        return UserPrincipal.create(user);
    }
}
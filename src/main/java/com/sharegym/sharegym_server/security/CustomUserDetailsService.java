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
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String emailOrId) throws UsernameNotFoundException {
        User user;

        // ID로 조회 시도 (JWT 토큰에서 추출한 경우)
        if (emailOrId.matches("\\d+")) {
            Long userId = Long.parseLong(emailOrId);
            user = userRepository.findById(userId)
                .orElseThrow(() ->
                    new UsernameNotFoundException("User not found with id: " + userId)
                );
        } else {
            // 이메일로 조회 (로그인 시)
            user = userRepository.findByEmail(emailOrId)
                .orElseThrow(() ->
                    new UsernameNotFoundException("User not found with email: " + emailOrId)
                );
        }

        return UserPrincipal.create(user);
    }
}
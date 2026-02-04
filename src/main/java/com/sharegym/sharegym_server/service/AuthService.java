package com.sharegym.sharegym_server.service;

import com.sharegym.sharegym_server.dto.request.LoginRequest;
import com.sharegym.sharegym_server.dto.request.RefreshTokenRequest;
import com.sharegym.sharegym_server.dto.request.SignUpRequest;
import com.sharegym.sharegym_server.dto.response.AuthResponse;
import com.sharegym.sharegym_server.dto.response.UserResponse;
import com.sharegym.sharegym_server.entity.User;
import com.sharegym.sharegym_server.exception.BusinessException;
import com.sharegym.sharegym_server.exception.ErrorCode;
import com.sharegym.sharegym_server.repository.UserRepository;
import com.sharegym.sharegym_server.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    /**
     * 회원가입
     */
    @Transactional
    public AuthResponse signUp(SignUpRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 사용자명 중복 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // 사용자 생성
        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .username(request.getUsername())
            .displayName(request.getDisplayName())
            .bio(request.getBio())
            .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getEmail());

        // 자동 로그인 처리
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        return createAuthResponse(authentication, savedUser);
    }

    /**
     * 로그인
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 인증 시도
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // FCM 토큰 업데이트
        if (request.getFcmToken() != null) {
            user.setFcmToken(request.getFcmToken());
            userRepository.save(user);
        }

        log.info("User logged in: {}", user.getEmail());
        return createAuthResponse(authentication, user);
    }

    /**
     * 토큰 갱신
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 리프레시 토큰 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 토큰에서 이메일 추출 (새로운 방식)
        String email = jwtProvider.getEmailFromToken(refreshToken);

        // 사용자 조회 (이메일로)
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 새로운 토큰 생성
        String newAccessToken = jwtProvider.generateAccessToken(user.getEmail(), user.getId());
        String newRefreshToken = jwtProvider.generateRefreshToken(user.getEmail(), user.getId());

        log.info("Token refreshed for user: {}", user.getEmail());

        return AuthResponse.of(
            newAccessToken,
            newRefreshToken,
            86400L, // 24시간
            UserResponse.from(user)
        );
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            // FCM 토큰 제거 (선택적)
            User user = userRepository.findById(Long.parseLong(auth.getName()))
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            user.setFcmToken(null);
            userRepository.save(user);

            log.info("User logged out: {}", user.getEmail());
        }
        SecurityContextHolder.clearContext();
    }

    /**
     * 현재 사용자 정보 조회
     * 프론트엔드 API 규격: GET /auth/me
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(Long.parseLong(auth.getName()))
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return UserResponse.from(user);
    }

    /**
     * 현재 사용자 정보 수정
     * 프론트엔드 API 규격: PATCH /auth/me
     */
    @Transactional
    public UserResponse updateCurrentUser(java.util.Map<String, Object> updates) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(Long.parseLong(auth.getName()))
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 각 필드별로 업데이트
        if (updates.containsKey("username")) {
            String newUsername = (String) updates.get("username");
            // 사용자명 중복 확인
            if (!user.getUsername().equals(newUsername) &&
                userRepository.existsByUsername(newUsername)) {
                throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
            }
            user.setUsername(newUsername);
        }

        if (updates.containsKey("displayName")) {
            user.setDisplayName((String) updates.get("displayName"));
        }

        if (updates.containsKey("bio")) {
            user.setBio((String) updates.get("bio"));
        }

        if (updates.containsKey("profileImageUrl")) {
            user.setProfileImageUrl((String) updates.get("profileImageUrl"));
        }

        if (updates.containsKey("height")) {
            user.setHeight(((Number) updates.get("height")).doubleValue());
        }

        if (updates.containsKey("weight")) {
            user.setWeight(((Number) updates.get("weight")).doubleValue());
        }

        User updatedUser = userRepository.save(user);
        log.info("User profile updated: {}", updatedUser.getEmail());

        return UserResponse.from(updatedUser);
    }

    /**
     * 인증 응답 생성
     */
    private AuthResponse createAuthResponse(Authentication authentication, User user) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        return AuthResponse.of(
            accessToken,
            refreshToken,
            86400L, // 24시간
            UserResponse.from(user)
        );
    }
}
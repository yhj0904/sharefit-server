package com.sharegym.sharegym_server.controller;

import com.sharegym.sharegym_server.dto.request.LoginRequest;
import com.sharegym.sharegym_server.dto.request.RefreshTokenRequest;
import com.sharegym.sharegym_server.dto.request.SignUpRequest;
import com.sharegym.sharegym_server.dto.response.ApiResponse;
import com.sharegym.sharegym_server.dto.response.AuthResponse;
import com.sharegym.sharegym_server.dto.response.UserResponse;
import com.sharegym.sharegym_server.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 인증 관련 컨트롤러
 * 프론트엔드 API 규격에 맞춘 응답 형식 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입 (백엔드 기존 경로)
     */
    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody SignUpRequest request) {
        log.info("Sign up request for email: {}", request.getEmail());
        AuthResponse response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response));
    }

    /**
     * 회원가입 (프론트엔드 경로)
     * 프론트엔드 API 규격: POST /auth/signup
     * 응답: { user: User, token: string }
     */
    @PostMapping("/signup")
    @Operation(summary = "회원가입 (프론트엔드)", description = "프론트엔드 규격에 맞춘 회원가입 API")
    public ResponseEntity<Map<String, Object>> signUp(@Valid @RequestBody SignUpRequest request) {
        log.info("Frontend sign up request for email: {}", request.getEmail());
        AuthResponse authResponse = authService.signUp(request);

        // 프론트엔드 응답 형식으로 변환
        Map<String, Object> response = new HashMap<>();
        response.put("user", authResponse.getUser());
        response.put("token", authResponse.getAccessToken());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 로그인
     * 프론트엔드 API 규격: POST /auth/login
     * 응답: { user: User, token: string }
     */
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.getEmail());
        AuthResponse authResponse = authService.login(request);

        // 프론트엔드 응답 형식으로 변환
        Map<String, Object> response = new HashMap<>();
        response.put("user", authResponse.getUser());
        response.put("token", authResponse.getAccessToken());

        return ResponseEntity.ok(response);
    }

    /**
     * 현재 사용자 정보 조회
     * 프론트엔드 API 규격: GET /auth/me
     * 응답: User
     */
    @GetMapping("/me")
    @Operation(summary = "현재 사용자 정보", description = "현재 로그인한 사용자 정보를 조회합니다.")
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.info("Get current user info");
        UserResponse user = authService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    /**
     * 현재 사용자 정보 수정
     * 프론트엔드 API 규격: PATCH /auth/me
     * 응답: User
     */
    @PatchMapping("/me")
    @Operation(summary = "사용자 정보 수정", description = "현재 사용자 정보를 수정합니다.")
    public ResponseEntity<UserResponse> updateCurrentUser(@RequestBody Map<String, Object> updates) {
        log.info("Update current user info");
        UserResponse user = authService.updateCurrentUser(updates);
        return ResponseEntity.ok(user);
    }

    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새로운 액세스 토큰을 발급받습니다.")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request");
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 로그아웃
     * 프론트엔드 API 규격: POST /auth/logout
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃 처리를 합니다.")
    public ResponseEntity<Void> logout() {
        log.info("Logout request");
        authService.logout();
        return ResponseEntity.ok().build();
    }
}
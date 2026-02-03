package com.sharegym.sharegym_server.controller;

import com.sharegym.sharegym_server.dto.request.UpdateProfileRequest;
import com.sharegym.sharegym_server.dto.response.ApiResponse;
import com.sharegym.sharegym_server.dto.response.UserResponse;
import com.sharegym.sharegym_server.security.CurrentUser;
import com.sharegym.sharegym_server.security.UserPrincipal;
import com.sharegym.sharegym_server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 관련 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    /**
     * 현재 사용자 프로필 조회
     */
    @GetMapping("/profile")
    @Operation(summary = "현재 사용자 프로필 조회", description = "로그인한 사용자의 프로필을 조회합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        log.info("Get current user profile: {}", userPrincipal.getId());
        UserResponse response = userService.getCurrentUser(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로필 수정
     */
    @PutMapping("/profile")
    @Operation(summary = "프로필 수정", description = "로그인한 사용자의 프로필을 수정합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
        @CurrentUser UserPrincipal userPrincipal,
        @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Update user profile: {}", userPrincipal.getId());
        UserResponse response = userService.updateProfile(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 사용자 프로필 조회 (ID)
     */
    @GetMapping("/{userId}")
    @Operation(summary = "사용자 프로필 조회", description = "특정 사용자의 프로필을 조회합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
        @PathVariable Long userId,
        @CurrentUser UserPrincipal userPrincipal) {
        log.info("Get user profile by id: {}", userId);
        UserResponse response = userService.getUserById(userId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 사용자 프로필 조회 (사용자명)
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "사용자명으로 프로필 조회", description = "사용자명으로 프로필을 조회합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(
        @PathVariable String username,
        @CurrentUser UserPrincipal userPrincipal) {
        log.info("Get user profile by username: {}", username);
        UserResponse response = userService.getUserByUsername(username, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 사용자 검색
     */
    @GetMapping("/search")
    @Operation(summary = "사용자 검색", description = "키워드로 사용자를 검색합니다.")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(
        @RequestParam String keyword,
        @CurrentUser UserPrincipal userPrincipal) {
        log.info("Search users with keyword: {}", keyword);
        List<UserResponse> response = userService.searchUsers(keyword, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 팔로우
     */
    @PostMapping("/follow/{userId}")
    @Operation(summary = "팔로우", description = "특정 사용자를 팔로우합니다.")
    public ResponseEntity<ApiResponse<Void>> followUser(
        @PathVariable Long userId,
        @CurrentUser UserPrincipal userPrincipal) {
        log.info("Follow user: {} by {}", userId, userPrincipal.getId());
        userService.followUser(userPrincipal.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 언팔로우
     */
    @DeleteMapping("/follow/{userId}")
    @Operation(summary = "언팔로우", description = "특정 사용자를 언팔로우합니다.")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(
        @PathVariable Long userId,
        @CurrentUser UserPrincipal userPrincipal) {
        log.info("Unfollow user: {} by {}", userId, userPrincipal.getId());
        userService.unfollowUser(userPrincipal.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 팔로워 목록 조회
     */
    @GetMapping("/{userId}/followers")
    @Operation(summary = "팔로워 목록 조회", description = "특정 사용자의 팔로워 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getFollowers(
        @PathVariable Long userId,
        @CurrentUser UserPrincipal userPrincipal) {
        log.info("Get followers of user: {}", userId);
        List<UserResponse> response = userService.getFollowers(userId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 팔로잉 목록 조회
     */
    @GetMapping("/{userId}/following")
    @Operation(summary = "팔로잉 목록 조회", description = "특정 사용자의 팔로잉 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getFollowing(
        @PathVariable Long userId,
        @CurrentUser UserPrincipal userPrincipal) {
        log.info("Get following of user: {}", userId);
        List<UserResponse> response = userService.getFollowing(userId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
package com.sharegym.sharegym_server.dto.response;

import com.sharegym.sharegym_server.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String username;
    private String displayName;
    private String profileImageUrl;
    private String bio;

    // 통계 정보
    private Integer workoutCount;
    private Integer followerCount;
    private Integer followingCount;
    private Integer currentStreak;
    private Integer maxStreak;

    // 상태 정보
    private Boolean isActive;
    private Boolean isVerified;
    private String role;

    // 팔로우 상태 (현재 사용자 기준)
    private Boolean isFollowing;
    private Boolean isFollower;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity를 DTO로 변환
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .username(user.getUsername())
            .displayName(user.getDisplayName())
            .profileImageUrl(user.getProfileImageUrl())
            .bio(user.getBio())
            .workoutCount(user.getWorkoutCount())
            .followerCount(user.getFollowerCount())
            .followingCount(user.getFollowingCount())
            .currentStreak(user.getCurrentStreak())
            .maxStreak(user.getMaxStreak())
            .isActive(user.getIsActive())
            .isVerified(user.getIsVerified())
            .role(user.getRole().name())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

    /**
     * Entity를 DTO로 변환 (팔로우 상태 포함)
     */
    public static UserResponse from(User user, boolean isFollowing, boolean isFollower) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .username(user.getUsername())
            .displayName(user.getDisplayName())
            .profileImageUrl(user.getProfileImageUrl())
            .bio(user.getBio())
            .workoutCount(user.getWorkoutCount())
            .followerCount(user.getFollowerCount())
            .followingCount(user.getFollowingCount())
            .currentStreak(user.getCurrentStreak())
            .maxStreak(user.getMaxStreak())
            .isActive(user.getIsActive())
            .isVerified(user.getIsVerified())
            .role(user.getRole().name())
            .isFollowing(isFollowing)
            .isFollower(isFollower)
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
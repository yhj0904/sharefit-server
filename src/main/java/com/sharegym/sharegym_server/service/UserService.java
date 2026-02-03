package com.sharegym.sharegym_server.service;

import com.sharegym.sharegym_server.dto.request.UpdateProfileRequest;
import com.sharegym.sharegym_server.dto.response.UserResponse;
import com.sharegym.sharegym_server.entity.User;
import com.sharegym.sharegym_server.exception.BusinessException;
import com.sharegym.sharegym_server.exception.ErrorCode;
import com.sharegym.sharegym_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 관련 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 현재 사용자 프로필 조회
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    /**
     * 사용자 프로필 조회 (ID)
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long targetUserId, Long currentUserId) {
        User targetUser = userRepository.findById(targetUserId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 현재 사용자와의 관계 확인
        if (currentUserId != null && !currentUserId.equals(targetUserId)) {
            User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            boolean isFollowing = currentUser.getFollowing().contains(targetUser);
            boolean isFollower = currentUser.getFollowers().contains(targetUser);

            return UserResponse.from(targetUser, isFollowing, isFollower);
        }

        return UserResponse.from(targetUser);
    }

    /**
     * 사용자 프로필 조회 (사용자명)
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username, Long currentUserId) {
        User targetUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 현재 사용자와의 관계 확인
        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            boolean isFollowing = currentUser.getFollowing().contains(targetUser);
            boolean isFollower = currentUser.getFollowers().contains(targetUser);

            return UserResponse.from(targetUser, isFollowing, isFollower);
        }

        return UserResponse.from(targetUser);
    }

    /**
     * 프로필 수정
     */
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 사용자명 변경 시 중복 확인
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
            }
            user.setUsername(request.getUsername());
        }

        // 필드 업데이트
        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (request.getFcmToken() != null) {
            user.setFcmToken(request.getFcmToken());
        }

        User updatedUser = userRepository.save(user);
        log.info("User profile updated: {}", updatedUser.getEmail());

        return UserResponse.from(updatedUser);
    }

    /**
     * 사용자 검색
     */
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String keyword, Long currentUserId) {
        List<User> users = userRepository.searchUsers(keyword);

        User currentUser = null;
        if (currentUserId != null) {
            currentUser = userRepository.findById(currentUserId).orElse(null);
        }

        final User finalCurrentUser = currentUser;
        return users.stream()
            .filter(u -> !u.getId().equals(currentUserId)) // 본인 제외
            .map(user -> {
                if (finalCurrentUser != null) {
                    boolean isFollowing = finalCurrentUser.getFollowing().contains(user);
                    boolean isFollower = finalCurrentUser.getFollowers().contains(user);
                    return UserResponse.from(user, isFollowing, isFollower);
                }
                return UserResponse.from(user);
            })
            .collect(Collectors.toList());
    }

    /**
     * 팔로우
     */
    @Transactional
    public void followUser(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "자기 자신을 팔로우할 수 없습니다.");
        }

        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        User targetUser = userRepository.findById(targetUserId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (currentUser.getFollowing().contains(targetUser)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이미 팔로우 중입니다.");
        }

        currentUser.follow(targetUser);
        userRepository.save(currentUser);
        userRepository.save(targetUser);

        log.info("User {} followed user {}", currentUser.getUsername(), targetUser.getUsername());
    }

    /**
     * 언팔로우
     */
    @Transactional
    public void unfollowUser(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "자기 자신을 언팔로우할 수 없습니다.");
        }

        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        User targetUser = userRepository.findById(targetUserId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!currentUser.getFollowing().contains(targetUser)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "팔로우하지 않은 사용자입니다.");
        }

        currentUser.unfollow(targetUser);
        userRepository.save(currentUser);
        userRepository.save(targetUser);

        log.info("User {} unfollowed user {}", currentUser.getUsername(), targetUser.getUsername());
    }

    /**
     * 팔로워 목록 조회
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getFollowers(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        User currentUser = null;
        if (currentUserId != null) {
            currentUser = userRepository.findById(currentUserId).orElse(null);
        }

        final User finalCurrentUser = currentUser;
        return user.getFollowers().stream()
            .map(follower -> {
                if (finalCurrentUser != null) {
                    boolean isFollowing = finalCurrentUser.getFollowing().contains(follower);
                    boolean isFollower = finalCurrentUser.getFollowers().contains(follower);
                    return UserResponse.from(follower, isFollowing, isFollower);
                }
                return UserResponse.from(follower);
            })
            .collect(Collectors.toList());
    }

    /**
     * 팔로잉 목록 조회
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getFollowing(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        User currentUser = null;
        if (currentUserId != null) {
            currentUser = userRepository.findById(currentUserId).orElse(null);
        }

        final User finalCurrentUser = currentUser;
        return user.getFollowing().stream()
            .map(following -> {
                if (finalCurrentUser != null) {
                    boolean isFollowing = finalCurrentUser.getFollowing().contains(following);
                    boolean isFollower = finalCurrentUser.getFollowers().contains(following);
                    return UserResponse.from(following, isFollowing, isFollower);
                }
                return UserResponse.from(following);
            })
            .collect(Collectors.toList());
    }
}
package com.sharegym.sharegym_server.controller;

import com.sharegym.sharegym_server.dto.request.CreateGroupRequest;
import com.sharegym.sharegym_server.dto.request.JoinGroupRequest;
import com.sharegym.sharegym_server.dto.request.ShareToGroupRequest;
import com.sharegym.sharegym_server.dto.response.*;
import com.sharegym.sharegym_server.security.CurrentUser;
import com.sharegym.sharegym_server.security.UserPrincipal;
import com.sharegym.sharegym_server.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 그룹 관련 API 컨트롤러
 *
 * Frontend API Contract:
 * - GET /users/{userId}/groups - 사용자 그룹 목록
 * - GET /groups/{groupId}/posts - 그룹 포스트 목록
 * - POST /groups - 그룹 생성
 * - POST /groups/join - 그룹 가입
 * - POST /groups/{groupId}/leave - 그룹 탈퇴
 * - POST /groups/{groupId}/posts - 그룹에 포스트 공유
 * - GET /groups/{groupId}/shared-cards - 공유 카드 목록
 * - POST /shared-cards - 공유 카드 생성
 */
@Slf4j
@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 사용자의 그룹 목록 조회
     * Frontend expects: Group[] or { data: Group[] }
     */
    @GetMapping("/users/{userId}/groups")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getUserGroups(
            @PathVariable Long userId,
            @CurrentUser UserPrincipal userPrincipal) {

        log.info("Getting groups for user: {} requested by: {}",
                 userId, userPrincipal.getId());

        // 다른 사용자의 그룹 목록은 볼 수 없음 (보안)
        if (!userId.equals(userPrincipal.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("다른 사용자의 그룹 목록은 조회할 수 없습니다."));
        }

        List<GroupResponse> groups = groupService.getUserGroups(userId);
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    /**
     * 그룹 생성
     * Frontend expects: Group or { data: Group }
     */
    @PostMapping("/groups")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody CreateGroupRequest request) {

        log.info("Creating group for user: {}", userPrincipal.getId());
        GroupResponse group = groupService.createGroup(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(group));
    }

    /**
     * 초대 코드로 그룹 가입
     */
    @PostMapping("/groups/join")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<GroupResponse>> joinGroup(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody JoinGroupRequest request) {

        log.info("User {} joining group with invite code: {}",
                 userPrincipal.getId(), request.getInviteCode());

        GroupResponse group = groupService.joinGroup(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(group));
    }

    /**
     * 그룹 탈퇴
     */
    @PostMapping("/groups/{groupId}/leave")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> leaveGroup(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long groupId) {

        log.info("User {} leaving group: {}", userPrincipal.getId(), groupId);
        groupService.leaveGroup(userPrincipal.getId(), groupId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 그룹 상세 조회
     */
    @GetMapping("/groups/{groupId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroup(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long groupId) {

        log.info("Getting group: {} for user: {}", groupId, userPrincipal.getId());
        GroupResponse group = groupService.getGroup(groupId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(group));
    }

    /**
     * 그룹 멤버 목록 조회
     */
    @GetMapping("/groups/{groupId}/members")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<GroupMemberResponse>>> getGroupMembers(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long groupId) {

        log.info("Getting members for group: {} requested by user: {}",
                 groupId, userPrincipal.getId());

        List<GroupMemberResponse> members = groupService.getGroupMembers(groupId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    /**
     * 그룹 포스트 목록 조회
     * Frontend expects: GroupPost[] or { data: GroupPost[] }
     */
    @GetMapping("/groups/{groupId}/posts")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<GroupPostResponse>>> getGroupPosts(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long groupId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Getting posts for group: {} requested by user: {}",
                 groupId, userPrincipal.getId());

        Page<GroupPostResponse> posts = groupService.getGroupPosts(groupId, userPrincipal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    /**
     * 그룹에 포스트 공유
     * Frontend expects: GroupPost or { data: GroupPost }
     */
    @PostMapping("/groups/{groupId}/posts")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<GroupPostResponse>> shareToGroup(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long groupId,
            @Valid @RequestBody ShareToGroupRequest request) {

        log.info("User {} sharing to group: {}", userPrincipal.getId(), groupId);

        // groupId를 request에 설정
        request.setGroupId(groupId);

        GroupPostResponse post = groupService.shareToGroup(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(post));
    }

    /**
     * 공개 그룹 목록 조회
     */
    @GetMapping("/groups")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<GroupResponse>>> getPublicGroups(
            @RequestParam(required = false, defaultValue = "recent") String sortBy,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Getting public groups sorted by: {}", sortBy);
        Page<GroupResponse> groups = groupService.getPublicGroups(sortBy, pageable);
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    /**
     * 그룹 검색
     */
    @GetMapping("/groups/search")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<GroupResponse>>> searchGroups(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Searching groups with keyword: {}", keyword);
        Page<GroupResponse> groups = groupService.searchGroups(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    /**
     * 그룹별 공유 카드 목록 조회
     * Frontend expects: SharedWorkoutCard[] or { data: SharedWorkoutCard[] }
     * TODO: SharedWorkoutCard 기능 구현 필요
     */
    @GetMapping("/groups/{groupId}/shared-cards")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSharedCards(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long groupId) {

        log.info("Getting shared cards for group: {} requested by user: {}",
                 groupId, userPrincipal.getId());

        // TODO: SharedWorkoutCard 기능 구현 필요
        Map<String, Object> result = new HashMap<>();
        result.put("cards", List.of());
        result.put("message", "공유 카드 기능은 준비 중입니다.");

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 공유 카드 생성
     * Frontend expects: SharedWorkoutCard or { data: SharedWorkoutCard }
     * TODO: SharedWorkoutCard 기능 구현 필요
     */
    @PostMapping("/shared-cards")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createSharedCard(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody Map<String, Object> request) {

        log.info("Creating shared card for user: {}", userPrincipal.getId());

        // TODO: SharedWorkoutCard 기능 구현 필요
        Map<String, Object> result = new HashMap<>();
        result.put("message", "공유 카드 기능은 준비 중입니다.");

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 공유 카드 완료
     * TODO: SharedWorkoutCard 기능 구현 필요
     */
    @PostMapping("/shared-cards/{cardId}/complete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> completeSharedCard(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long cardId,
            @RequestBody Map<String, Object> request) {

        log.info("Completing shared card: {} by user: {}", cardId, userPrincipal.getId());

        // TODO: SharedWorkoutCard 기능 구현 필요
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
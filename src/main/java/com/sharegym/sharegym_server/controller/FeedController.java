package com.sharegym.sharegym_server.controller;

import com.sharegym.sharegym_server.dto.request.CreateCommentRequest;
import com.sharegym.sharegym_server.dto.request.CreateFeedRequest;
import com.sharegym.sharegym_server.dto.response.ApiResponse;
import com.sharegym.sharegym_server.dto.response.CommentResponse;
import com.sharegym.sharegym_server.dto.response.FeedResponse;
import com.sharegym.sharegym_server.security.CurrentUser;
import com.sharegym.sharegym_server.security.UserPrincipal;
import com.sharegym.sharegym_server.service.FeedService;
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
 * 피드 관련 API 컨트롤러
 *
 * Frontend API Contract:
 * - GET /feed?filter=all|following|groups&cursor=&limit=20
 * - POST /feed - 피드 작성
 * - POST /feed/with-workout - 운동과 함께 피드 작성
 * - POST /feed/{id}/like - 좋아요
 * - DELETE /feed/{id}/like - 좋아요 취소
 * - POST /feed/{id}/comments - 댓글 작성
 * - DELETE /feed/{id} - 피드 삭제
 * - DELETE /feed/{id}/comments/{commentId} - 댓글 삭제
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    /**
     * 피드 목록 조회 (필터별)
     * Frontend expects: { items: FeedItem[], nextCursor: string, hasMore: boolean }
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFeeds(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(required = false, defaultValue = "all") String filter,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "20") int limit,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Getting feeds with filter: {} for user: {}", filter, userPrincipal.getId());

        Page<FeedResponse> feedPage;

        switch (filter) {
            case "following":
                feedPage = feedService.getFollowingFeeds(userPrincipal.getId(), pageable);
                break;
            case "groups":
                // TODO: Group 피드 구현 필요
                feedPage = feedService.getAllFeeds(userPrincipal.getId(), pageable);
                break;
            case "all":
            default:
                feedPage = feedService.getAllFeeds(userPrincipal.getId(), pageable);
                break;
        }

        // Frontend가 기대하는 형식으로 변환
        Map<String, Object> result = new HashMap<>();
        result.put("items", feedPage.getContent());
        result.put("nextCursor", feedPage.hasNext() ? String.valueOf(feedPage.getNumber() + 1) : null);
        result.put("hasMore", feedPage.hasNext());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 단일 피드 조회
     */
    @GetMapping("/{feedId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<FeedResponse>> getFeed(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long feedId) {

        log.info("Getting feed: {} for user: {}", feedId, userPrincipal.getId());
        FeedResponse feed = feedService.getFeed(feedId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(feed));
    }

    /**
     * 피드 작성 (일반)
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<FeedResponse>> createFeed(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody CreateFeedRequest request) {

        log.info("Creating feed for user: {}", userPrincipal.getId());
        FeedResponse feed = feedService.createFeed(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(feed));
    }

    /**
     * 피드 작성 (운동과 함께)
     * Frontend에서 운동 완료 후 피드 공유시 호출
     */
    @PostMapping("/with-workout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<FeedResponse>> createFeedWithWorkout(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody CreateFeedRequest request) {

        log.info("Creating feed with workout for user: {}", userPrincipal.getId());

        // workoutId가 필수
        if (request.getWorkoutId() == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("운동 정보가 필요합니다."));
        }

        FeedResponse feed = feedService.createFeed(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(feed));
    }

    /**
     * 피드 삭제
     */
    @DeleteMapping("/{feedId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteFeed(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long feedId) {

        log.info("Deleting feed: {} by user: {}", feedId, userPrincipal.getId());
        feedService.deleteFeed(userPrincipal.getId(), feedId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 좋아요 토글 (좋아요/취소)
     * Frontend expects: { liked: boolean, likesCount: number }
     */
    @PostMapping("/{feedId}/like")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleLike(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long feedId) {

        log.info("Toggling like on feed: {} by user: {}", feedId, userPrincipal.getId());

        // 현재 좋아요 상태 확인 후 토글
        FeedResponse feed = feedService.getFeed(feedId, userPrincipal.getId());
        Map<String, Object> result = new HashMap<>();

        if (feed.getIsLikedByMe()) {
            // 이미 좋아요 상태 -> 취소
            feedService.unlikeFeed(userPrincipal.getId(), feedId);
            result.put("liked", false);
            result.put("likesCount", feed.getLikesCount() - 1);
        } else {
            // 좋아요 안한 상태 -> 좋아요
            feedService.likeFeed(userPrincipal.getId(), feedId);
            result.put("liked", true);
            result.put("likesCount", feed.getLikesCount() + 1);
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 좋아요 취소
     * Backend specific endpoint (Frontend uses toggle)
     */
    @DeleteMapping("/{feedId}/like")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> unlikeFeed(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long feedId) {

        log.info("Unliking feed: {} by user: {}", feedId, userPrincipal.getId());
        feedService.unlikeFeed(userPrincipal.getId(), feedId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 댓글 작성
     */
    @PostMapping("/{feedId}/comments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long feedId,
            @Valid @RequestBody CreateCommentRequest request) {

        log.info("Creating comment on feed: {} by user: {}", feedId, userPrincipal.getId());
        CommentResponse comment = feedService.createComment(userPrincipal.getId(), feedId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(comment));
    }

    /**
     * 댓글 목록 조회
     */
    @GetMapping("/{feedId}/comments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable Long feedId) {

        log.info("Getting comments for feed: {}", feedId);
        List<CommentResponse> comments = feedService.getFeedComments(feedId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{feedId}/comments/{commentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long feedId,
            @PathVariable Long commentId) {

        log.info("Deleting comment: {} on feed: {} by user: {}", commentId, feedId, userPrincipal.getId());
        feedService.deleteComment(userPrincipal.getId(), feedId, commentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 사용자별 피드 목록 조회
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<FeedResponse>>> getUserFeeds(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Getting feeds for user: {} requested by: {}", userId, userPrincipal.getId());
        Page<FeedResponse> feeds = feedService.getUserFeeds(userId, userPrincipal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(feeds));
    }
}
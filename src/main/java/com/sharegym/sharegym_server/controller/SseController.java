package com.sharegym.sharegym_server.controller;

import com.sharegym.sharegym_server.security.CurrentUser;
import com.sharegym.sharegym_server.security.UserPrincipal;
import com.sharegym.sharegym_server.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE (Server-Sent Events) 컨트롤러
 * 실시간 알림 및 이벤트 스트리밍 엔드포인트
 */
@Slf4j
@RestController
@RequestMapping("/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseEmitterService sseEmitterService;

    /**
     * 사용자 개인 알림 구독
     * - 응원 알림
     * - 팔로우 알림
     * - 그룹 초대 등
     */
    @GetMapping(value = "/user/me", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('USER')")
    public SseEmitter subscribeUserEvents(
            @CurrentUser UserPrincipal userPrincipal) {

        log.info("User {} subscribing to personal events", userPrincipal.getId());
        return sseEmitterService.createUserEmitter(userPrincipal.getId());
    }

    /**
     * 특정 사용자 이벤트 구독 (다른 사용자의 운동 상태 등)
     */
    @GetMapping(value = "/user/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('USER')")
    public SseEmitter subscribeToUser(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long userId) {

        log.info("User {} subscribing to user {} events", userPrincipal.getId(), userId);
        // 팔로우 관계 확인 등 추가 검증 필요
        return sseEmitterService.createUserEmitter(userId);
    }

    /**
     * 운동 실시간 구독
     * - 세트/무게 업데이트
     * - 운동 완료 알림
     * - 실시간 응원
     */
    @GetMapping(value = "/workout/{workoutId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('USER')")
    public SseEmitter subscribeToWorkout(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long workoutId) {

        log.info("User {} subscribing to workout {}", userPrincipal.getId(), workoutId);
        return sseEmitterService.subscribeToWorkout(workoutId, userPrincipal.getId());
    }

    /**
     * 그룹 활동 구독
     * - 새 멤버 가입
     * - 그룹 포스트
     * - 그룹 챌린지
     */
    @GetMapping(value = "/group/{groupId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('USER')")
    public SseEmitter subscribeToGroup(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long groupId) {

        log.info("User {} subscribing to group {}", userPrincipal.getId(), groupId);
        // TODO: 그룹 멤버십 확인
        return sseEmitterService.subscribeToGroup(groupId, userPrincipal.getId());
    }

    /**
     * 전체 피드 실시간 구독
     * - 새 피드 포스트
     * - 인기 운동
     * - 팔로우한 사용자 활동
     */
    @GetMapping(value = "/feed", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('USER')")
    public SseEmitter subscribeToFeed(
            @CurrentUser UserPrincipal userPrincipal) {

        log.info("User {} subscribing to feed events", userPrincipal.getId());
        return sseEmitterService.subscribeToFeed(userPrincipal.getId());
    }

    /**
     * 연결 상태 확인 (헬스체크)
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('USER')")
    public String checkSseHealth() {
        return "SSE service is running";
    }
}
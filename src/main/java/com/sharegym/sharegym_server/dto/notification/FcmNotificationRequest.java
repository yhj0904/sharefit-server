package com.sharegym.sharegym_server.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * FCM 푸시 알림 요청 DTO
 * Redis Pub/Sub을 통해 전달되는 메시지 형식
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmNotificationRequest {

    /**
     * 알림을 받을 사용자 ID
     */
    private Long userId;

    /**
     * FCM 디바이스 토큰
     * null인 경우 userId로 토큰 조회
     */
    private String deviceToken;

    /**
     * 알림 제목
     */
    private String title;

    /**
     * 알림 내용
     */
    private String body;

    /**
     * 알림 타입
     * (WORKOUT, FEED, COMMENT, LIKE, FOLLOW, GROUP, CHEER 등)
     */
    private NotificationType notificationType;

    /**
     * 추가 데이터 (클라이언트에서 처리할 추가 정보)
     * 예: targetId, groupId, workoutId 등
     */
    private Map<String, String> data;

    /**
     * 알림 우선순위
     */
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    /**
     * 알림 타입 enum
     */
    public enum NotificationType {
        WORKOUT_START,      // 운동 시작 알림
        WORKOUT_COMPLETE,   // 운동 완료 알림
        CHEER,             // 응원 알림
        FEED_NEW,          // 새 피드 알림
        FEED_LIKE,         // 피드 좋아요 알림
        FEED_COMMENT,      // 피드 댓글 알림
        FOLLOW,            // 팔로우 알림
        GROUP_INVITE,      // 그룹 초대 알림
        GROUP_POST,        // 그룹 포스트 알림
        GROUP_JOIN,        // 그룹 가입 알림
        ROUTINE_REMINDER   // 루틴 리마인더 알림
    }

    /**
     * 알림 우선순위 enum
     */
    public enum Priority {
        HIGH,
        NORMAL,
        LOW
    }
}
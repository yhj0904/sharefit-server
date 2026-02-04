package com.sharegym.sharegym_server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharegym.sharegym_server.dto.notification.*;
import com.sharegym.sharegym_server.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 알림 서비스
 * 각종 이벤트 발생 시 SSE를 통해 실시간 알림 전송
 * SSE 미연결 사용자는 Redis Pub/Sub을 통해 FCM 푸시 알림 전송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SseEmitterService sseEmitterService;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private FCMService fcmService;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 운동 시작 알림
     */
    @Async
    public void notifyWorkoutStart(Workout workout) {
        WorkoutNotification notification = WorkoutNotification.builder()
            .workoutId(workout.getId())
            .userId(workout.getUser().getId())
            .userName(workout.getUser().getDisplayName())
            .workoutName(workout.getWorkoutName())
            .status("STARTED")
            .message(workout.getUser().getDisplayName() + "님이 운동을 시작했습니다.")
            .build();

        // 팔로워들에게 알림
        workout.getUser().getFollowers().forEach(follower -> {
            sendNotificationWithFallback(
                follower.getId(),
                "workout:start",
                notification,
                workout.getUser().getDisplayName() + "님이 운동을 시작했습니다.",
                workout.getUser().getDisplayName() + "님이 " + workout.getWorkoutName() + " 운동을 시작했습니다.",
                FcmNotificationRequest.NotificationType.WORKOUT_START,
                Map.of("workoutId", String.valueOf(workout.getId()))
            );
        });

        // 운동 구독자들에게 알림
        sseEmitterService.sendToWorkoutSubscribers(workout.getId(), "workout:start", notification);

        log.info("Workout start notification sent for workout {}", workout.getId());
    }

    /**
     * 운동 업데이트 알림 (세트 완료 등)
     */
    @Async
    public void notifyWorkoutUpdate(Workout workout, String updateType, Object details) {
        WorkoutUpdateNotification notification = WorkoutUpdateNotification.builder()
            .workoutId(workout.getId())
            .userId(workout.getUser().getId())
            .userName(workout.getUser().getDisplayName())
            .updateType(updateType)
            .details(details)
            .build();

        // 운동 구독자들에게 알림
        sseEmitterService.sendToWorkoutSubscribers(workout.getId(), "workout:update", notification);

        log.debug("Workout update notification sent for workout {}", workout.getId());
    }

    /**
     * 운동 완료 알림
     */
    @Async
    public void notifyWorkoutComplete(Workout workout) {
        WorkoutNotification notification = WorkoutNotification.builder()
            .workoutId(workout.getId())
            .userId(workout.getUser().getId())
            .userName(workout.getUser().getDisplayName())
            .workoutName(workout.getWorkoutName())
            .status("COMPLETED")
            .totalSets(workout.getTotalSets())
            .totalVolume(workout.getTotalVolume())
            .duration(workout.getDuration())
            .message(workout.getUser().getDisplayName() + "님이 운동을 완료했습니다!")
            .build();

        String title = "운동 완료";
        String body = String.format("%s님이 %d분간 운동을 완료했습니다!",
                workout.getUser().getDisplayName(), workout.getDuration());

        // 팔로워들에게 알림
        workout.getUser().getFollowers().forEach(follower -> {
            sendNotificationWithFallback(
                follower.getId(),
                "workout:complete",
                notification,
                title,
                body,
                FcmNotificationRequest.NotificationType.WORKOUT_COMPLETE,
                Map.of("workoutId", String.valueOf(workout.getId()))
            );
        });

        // 운동 구독자들에게 알림
        sseEmitterService.sendToWorkoutSubscribers(workout.getId(), "workout:complete", notification);

        log.info("Workout complete notification sent for workout {}", workout.getId());
    }

    /**
     * 응원 알림
     */
    @Async
    public void notifyCheer(Long targetUserId, Long fromUserId, String fromUserName, String message) {
        CheerNotification notification = CheerNotification.builder()
            .fromUserId(fromUserId)
            .fromUserName(fromUserName)
            .message(message)
            .build();

        // 대상 사용자에게 알림 (SSE 실패시 FCM 폴백)
        sendNotificationWithFallback(
            targetUserId,
            "cheer",
            notification,
            "응원 도착!",
            fromUserName + "님이 응원을 보냈습니다! 💪",
            FcmNotificationRequest.NotificationType.CHEER,
            Map.of("fromUserId", String.valueOf(fromUserId), "fromUserName", fromUserName)
        );

        log.info("Cheer notification sent from {} to {}", fromUserId, targetUserId);
    }

    /**
     * 새 피드 알림
     */
    @Async
    public void notifyNewFeed(Feed feed) {
        FeedNotification notification = FeedNotification.builder()
            .feedId(feed.getId())
            .userId(feed.getUser().getId())
            .userName(feed.getUser().getDisplayName())
            .userProfileImage(feed.getUser().getProfileImageUrl())
            .content(feed.getContent())
            .hasWorkout(feed.getWorkout() != null)
            .workoutId(feed.getWorkout() != null ? feed.getWorkout().getId() : null)
            .build();

        // 전체 피드 구독자들에게 알림
        sseEmitterService.sendToFeedSubscribers("feed:new", notification);

        // 팔로워들에게 개별 알림
        feed.getUser().getFollowers().forEach(follower -> {
            sseEmitterService.sendToUser(follower.getId(), "feed:following", notification);
        });

        // 그룹 공유인 경우 그룹 멤버들에게 알림
        if (feed.getSharedGroup() != null) {
            sseEmitterService.sendToGroupSubscribers(
                feed.getSharedGroup().getId(),
                "group:post",
                notification
            );
        }

        log.info("New feed notification sent for feed {}", feed.getId());
    }

    /**
     * 피드 좋아요 알림
     */
    @Async
    public void notifyFeedLike(Feed feed, User liker) {
        LikeNotification notification = LikeNotification.builder()
            .feedId(feed.getId())
            .likerId(liker.getId())
            .likerName(liker.getDisplayName())
            .likerProfileImage(liker.getProfileImageUrl())
            .build();

        // 피드 작성자에게 알림 (본인이 아닌 경우)
        if (!feed.getUser().getId().equals(liker.getId())) {
            sendNotificationWithFallback(
                feed.getUser().getId(),
                "feed:like",
                notification,
                "좋아요",
                liker.getDisplayName() + "님이 회원님의 피드를 좋아합니다",
                FcmNotificationRequest.NotificationType.FEED_LIKE,
                Map.of("feedId", String.valueOf(feed.getId()), "likerId", String.valueOf(liker.getId()))
            );
        }

        log.debug("Like notification sent for feed {}", feed.getId());
    }

    /**
     * 피드 댓글 알림
     */
    @Async
    public void notifyFeedComment(Feed feed, FeedComment comment) {
        CommentNotification notification = CommentNotification.builder()
            .feedId(feed.getId())
            .commentId(comment.getId())
            .commenterId(comment.getUser().getId())
            .commenterName(comment.getUser().getDisplayName())
            .commenterProfileImage(comment.getUser().getProfileImageUrl())
            .content(comment.getContent())
            .build();

        String title = "새 댓글";
        String body = comment.getUser().getDisplayName() + ": " +
                (comment.getContent().length() > 50 ? comment.getContent().substring(0, 50) + "..." : comment.getContent());

        // 피드 작성자에게 알림 (본인이 아닌 경우)
        if (!feed.getUser().getId().equals(comment.getUser().getId())) {
            sendNotificationWithFallback(
                feed.getUser().getId(),
                "feed:comment",
                notification,
                title,
                body,
                FcmNotificationRequest.NotificationType.FEED_COMMENT,
                Map.of("feedId", String.valueOf(feed.getId()), "commentId", String.valueOf(comment.getId()))
            );
        }

        // 부모 댓글 작성자에게 알림 (대댓글인 경우)
        if (comment.getParentComment() != null &&
            !comment.getParentComment().getUser().getId().equals(comment.getUser().getId())) {
            sendNotificationWithFallback(
                comment.getParentComment().getUser().getId(),
                "comment:reply",
                notification,
                "대댓글",
                comment.getUser().getDisplayName() + "님이 회원님의 댓글에 답글을 달았습니다",
                FcmNotificationRequest.NotificationType.FEED_COMMENT,
                Map.of("feedId", String.valueOf(feed.getId()), "commentId", String.valueOf(comment.getId()))
            );
        }

        log.debug("Comment notification sent for feed {}", feed.getId());
    }

    /**
     * 그룹 가입 알림
     */
    @Async
    public void notifyGroupJoin(Group group, User newMember) {
        GroupNotification notification = GroupNotification.builder()
            .groupId(group.getId())
            .groupName(group.getName())
            .userId(newMember.getId())
            .userName(newMember.getDisplayName())
            .eventType("MEMBER_JOIN")
            .message(newMember.getDisplayName() + "님이 그룹에 가입했습니다.")
            .build();

        // 그룹 구독자들에게 알림
        sseEmitterService.sendToGroupSubscribers(group.getId(), "group:join", notification);

        log.info("Group join notification sent for group {}", group.getId());
    }

    /**
     * 그룹 포스트 알림
     */
    @Async
    public void notifyGroupPost(Group group, Feed feed) {
        GroupPostNotification notification = GroupPostNotification.builder()
            .groupId(group.getId())
            .groupName(group.getName())
            .feedId(feed.getId())
            .userId(feed.getUser().getId())
            .userName(feed.getUser().getDisplayName())
            .content(feed.getContent())
            .hasWorkout(feed.getWorkout() != null)
            .build();

        // 그룹 구독자들에게 알림
        sseEmitterService.sendToGroupSubscribers(group.getId(), "group:post", notification);

        log.info("Group post notification sent for group {}", group.getId());
    }

    /**
     * 팔로우 알림
     */
    @Async
    public void notifyFollow(User follower, User following) {
        FollowNotification notification = FollowNotification.builder()
            .followerId(follower.getId())
            .followerName(follower.getDisplayName())
            .followerProfileImage(follower.getProfileImageUrl())
            .build();

        // 팔로우 대상에게 알림
        sendNotificationWithFallback(
            following.getId(),
            "follow",
            notification,
            "새 팔로워",
            follower.getDisplayName() + "님이 회원님을 팔로우하기 시작했습니다",
            FcmNotificationRequest.NotificationType.FOLLOW,
            Map.of("followerId", String.valueOf(follower.getId()), "followerName", follower.getDisplayName())
        );

        log.info("Follow notification sent from {} to {}", follower.getId(), following.getId());
    }

    /**
     * SSE 연결 여부를 확인하고 FCM으로 폴백하는 헬퍼 메서드
     */
    private void sendNotificationWithFallback(
            Long userId,
            String eventType,
            Object notification,
            String fcmTitle,
            String fcmBody,
            FcmNotificationRequest.NotificationType fcmType,
            Map<String, String> fcmData) {

        // SSE 연결 확인 및 전송 시도
        boolean sseSuccess = sseEmitterService.sendToUser(userId, eventType, notification);

        // SSE 연결이 없으면 FCM으로 푸시 알림 전송
        if (!sseSuccess) {
            sendFcmNotification(userId, fcmTitle, fcmBody, fcmType, fcmData);
        }
    }

    /**
     * FCM 푸시 알림 전송
     * Redis Pub/Sub 또는 직접 FCMService 호출
     */
    private void sendFcmNotification(
            Long userId,
            String title,
            String body,
            FcmNotificationRequest.NotificationType type,
            Map<String, String> data) {

        // 1. FCMService가 사용 가능하면 직접 호출
        if (fcmService != null) {
            try {
                FcmNotificationRequest fcmRequest = FcmNotificationRequest.builder()
                        .userId(userId)
                        .title(title)
                        .body(body)
                        .notificationType(type)
                        .data(data != null ? data : new HashMap<>())
                        .priority(FcmNotificationRequest.Priority.NORMAL)
                        .build();

                fcmService.sendNotification(fcmRequest);
                log.debug("FCM notification sent directly for user {}", userId);
                return;
            } catch (Exception e) {
                log.error("Failed to send FCM notification directly for user {}: {}", userId, e.getMessage());
            }
        }

        // 2. Redis가 사용 가능하면 Pub/Sub 사용 (분산 환경)
        if (redisTemplate != null) {
            try {
                FcmNotificationRequest fcmRequest = FcmNotificationRequest.builder()
                        .userId(userId)
                        .title(title)
                        .body(body)
                        .notificationType(type)
                        .data(data != null ? data : new HashMap<>())
                        .priority(FcmNotificationRequest.Priority.NORMAL)
                        .build();

                // Redis Pub/Sub으로 FCM 알림 요청 전송
                String message = objectMapper.writeValueAsString(fcmRequest);
                redisTemplate.convertAndSend("notification:fcm", message);

                log.debug("FCM notification request published via Redis for user {}", userId);
            } catch (Exception e) {
                log.error("Failed to send FCM notification via Redis for user {}: {}", userId, e.getMessage());
            }
        } else {
            log.debug("Neither FCMService nor Redis available - FCM notification skipped for user {}", userId);
        }
    }
}
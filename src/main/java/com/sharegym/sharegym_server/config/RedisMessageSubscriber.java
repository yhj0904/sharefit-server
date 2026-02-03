package com.sharegym.sharegym_server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharegym.sharegym_server.dto.notification.FcmNotificationRequest;
import com.sharegym.sharegym_server.service.FCMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Redis Pub/Sub 메시지 구독자
 * - FCM 알림 메시지를 수신하여 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"prod"})  // Only enable in production
public class RedisMessageSubscriber {

    private final FCMService fcmService;
    private final ObjectMapper objectMapper;

    /**
     * FCM 알림 메시지 처리
     * SSE에 연결되지 않은 사용자에게 FCM 푸시 알림 전송
     */
    public void handleFcmNotification(String message) {
        try {
            log.debug("FCM 알림 메시지 수신: {}", message);

            // JSON 문자열을 FcmNotificationRequest 객체로 변환
            FcmNotificationRequest request = objectMapper.readValue(message, FcmNotificationRequest.class);

            // FCM 서비스를 통해 푸시 알림 전송
            fcmService.sendNotification(request);

            log.info("FCM 알림 전송 완료 - userId: {}, type: {}",
                    request.getUserId(), request.getNotificationType());

        } catch (Exception e) {
            log.error("FCM 알림 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
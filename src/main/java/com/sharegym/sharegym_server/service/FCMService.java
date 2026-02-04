package com.sharegym.sharegym_server.service;

import com.google.firebase.messaging.*;
import com.sharegym.sharegym_server.dto.notification.FcmNotificationRequest;
import com.sharegym.sharegym_server.entity.User;
import com.sharegym.sharegym_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FCM (Firebase Cloud Messaging) 서비스
 * 푸시 알림 전송을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"prod"})  // Only enable in production
public class FCMService {

    @Autowired(required = false)
    private FirebaseMessaging firebaseMessaging;

    private final UserRepository userRepository;

    /**
     * 단일 사용자에게 푸시 알림 전송
     */
    public void sendNotification(FcmNotificationRequest request) {
        if (firebaseMessaging == null) {
            log.warn("FirebaseMessaging이 초기화되지 않았습니다. 푸시 알림을 전송할 수 없습니다.");
            return;
        }

        try {
            String deviceToken = request.getDeviceToken();

            // 디바이스 토큰이 없으면 userId로 조회
            if (deviceToken == null || deviceToken.isEmpty()) {
                User user = userRepository.findById(request.getUserId())
                        .orElse(null);

                if (user == null || user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
                    log.debug("FCM 토큰이 없는 사용자입니다. userId: {}", request.getUserId());
                    return;
                }
                deviceToken = user.getFcmToken();
            }

            // FCM 메시지 구성
            Message message = buildMessage(deviceToken, request);

            // 메시지 전송
            String response = firebaseMessaging.send(message);
            log.info("FCM 푸시 알림 전송 성공. userId: {}, messageId: {}",
                    request.getUserId(), response);

        } catch (FirebaseMessagingException e) {
            handleFirebaseException(e, request);
        } catch (Exception e) {
            log.error("FCM 푸시 알림 전송 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 다수의 사용자에게 푸시 알림 전송 (배치)
     */
    public void sendBatchNotifications(List<FcmNotificationRequest> requests) {
        if (firebaseMessaging == null || requests.isEmpty()) {
            return;
        }

        try {
            // FCM 토큰이 있는 사용자만 필터링
            List<Message> messages = requests.stream()
                    .map(this::buildMessageSafely)
                    .filter(msg -> msg != null)
                    .collect(Collectors.toList());

            if (messages.isEmpty()) {
                log.debug("전송할 FCM 메시지가 없습니다.");
                return;
            }

            // 배치 전송
            BatchResponse batchResponse = firebaseMessaging.sendAll(messages);

            log.info("FCM 배치 알림 전송 완료. 성공: {}, 실패: {}",
                    batchResponse.getSuccessCount(),
                    batchResponse.getFailureCount());

            // 실패한 메시지 로깅
            if (batchResponse.getFailureCount() > 0) {
                batchResponse.getResponses().forEach((resp) -> {
                    if (!resp.isSuccessful()) {
                        log.warn("FCM 메시지 전송 실패: {}", resp.getException().getMessage());
                    }
                });
            }

        } catch (Exception e) {
            log.error("FCM 배치 알림 전송 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 토픽 구독
     */
    public void subscribeToTopic(String deviceToken, String topic) {
        if (firebaseMessaging == null) {
            return;
        }

        try {
            TopicManagementResponse response = firebaseMessaging
                    .subscribeToTopic(List.of(deviceToken), topic);

            log.info("토픽 구독 성공. topic: {}, successCount: {}",
                    topic, response.getSuccessCount());

        } catch (Exception e) {
            log.error("토픽 구독 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 토픽 구독 해제
     */
    public void unsubscribeFromTopic(String deviceToken, String topic) {
        if (firebaseMessaging == null) {
            return;
        }

        try {
            TopicManagementResponse response = firebaseMessaging
                    .unsubscribeFromTopic(List.of(deviceToken), topic);

            log.info("토픽 구독 해제 성공. topic: {}, successCount: {}",
                    topic, response.getSuccessCount());

        } catch (Exception e) {
            log.error("토픽 구독 해제 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 토픽으로 알림 전송
     */
    public void sendToTopic(String topic, String title, String body, Map<String, String> data) {
        if (firebaseMessaging == null) {
            return;
        }

        try {
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data != null ? data : new HashMap<>())
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("토픽 메시지 전송 성공. topic: {}, messageId: {}", topic, response);

        } catch (Exception e) {
            log.error("토픽 메시지 전송 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * FCM 메시지 생성
     */
    private Message buildMessage(String deviceToken, FcmNotificationRequest request) {
        // 기본 메시지 빌더
        Message.Builder messageBuilder = Message.builder()
                .setToken(deviceToken);

        // 알림 설정 (iOS/Android 알림창에 표시)
        Notification notification = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody())
                .build();
        messageBuilder.setNotification(notification);

        // 추가 데이터 설정
        Map<String, String> data = request.getData();
        if (data == null) {
            data = new HashMap<>();
        }
        data.put("notificationType", request.getNotificationType().toString());
        data.put("userId", String.valueOf(request.getUserId()));
        messageBuilder.putAllData(data);

        // 우선순위 설정
        if (request.getPriority() == FcmNotificationRequest.Priority.HIGH) {
            messageBuilder.setAndroidConfig(AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .build());
            messageBuilder.setApnsConfig(ApnsConfig.builder()
                    .putHeader("apns-priority", "10")
                    .build());
        }

        return messageBuilder.build();
    }

    /**
     * 안전한 메시지 생성 (배치 전송용)
     */
    private Message buildMessageSafely(FcmNotificationRequest request) {
        try {
            String deviceToken = request.getDeviceToken();

            if (deviceToken == null || deviceToken.isEmpty()) {
                User user = userRepository.findById(request.getUserId())
                        .orElse(null);

                if (user == null || user.getFcmToken() == null) {
                    return null;
                }
                deviceToken = user.getFcmToken();
            }

            return buildMessage(deviceToken, request);
        } catch (Exception e) {
            log.warn("메시지 생성 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Firebase 예외 처리
     */
    private void handleFirebaseException(FirebaseMessagingException e,
                                          FcmNotificationRequest request) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();

        if (errorCode == MessagingErrorCode.UNREGISTERED ||
                errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
            // 유효하지 않은 토큰 - DB에서 제거
            log.warn("유효하지 않은 FCM 토큰. userId: {}, error: {}",
                    request.getUserId(), e.getMessage());

            userRepository.findById(request.getUserId())
                    .ifPresent(user -> {
                        user.setFcmToken(null);
                        userRepository.save(user);
                    });
        } else {
            log.error("FCM 전송 실패. errorCode: {}, message: {}",
                    errorCode, e.getMessage());
        }
    }

    /**
     * 사용자의 FCM 토큰 업데이트
     */
    @Transactional
    public void updateFcmToken(Long userId, String fcmToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setFcmToken(fcmToken);
        userRepository.save(user);
        log.debug("FCM 토큰 업데이트 완료. userId: {}", userId);
    }

    /**
     * 사용자의 FCM 토큰 삭제 (로그아웃 시)
     */
    @Transactional
    public void removeFcmToken(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setFcmToken(null);
            userRepository.save(user);
            log.debug("FCM 토큰 삭제 완료. userId: {}", userId);
        }
    }
}
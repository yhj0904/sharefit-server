package com.sharegym.sharegym_server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SSE Emitter 관리 서비스
 * 클라이언트별 SSE 연결을 관리하고 이벤트를 전송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SseEmitterService {

    // 타임아웃 시간 (1시간)
    private static final Long DEFAULT_TIMEOUT = 60L * 60 * 1000;

    // 사용자별 SSE Emitter 저장소
    private final Map<String, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    // 운동 세션별 구독자 목록
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> workoutEmitters = new ConcurrentHashMap<>();

    // 그룹별 구독자 목록
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> groupEmitters = new ConcurrentHashMap<>();

    // 전체 피드 구독자 목록
    private final CopyOnWriteArrayList<SseEmitter> feedEmitters = new CopyOnWriteArrayList<>();

    /**
     * 사용자별 SSE 연결 생성
     */
    public SseEmitter createUserEmitter(Long userId) {
        String emitterId = makeUserEmitterId(userId);
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        userEmitters.put(emitterId, emitter);

        // 연결 완료/타임아웃/에러 시 정리
        emitter.onCompletion(() -> removeUserEmitter(emitterId));
        emitter.onTimeout(() -> removeUserEmitter(emitterId));
        emitter.onError((e) -> removeUserEmitter(emitterId));

        // 초기 연결 이벤트 전송 (연결 확인용)
        try {
            emitter.send(SseEmitter.event()
                .id(emitterId)
                .name("connect")
                .data("SSE Connected"));
        } catch (IOException e) {
            log.error("Failed to send connect event", e);
        }

        log.info("SSE connection created for user: {}", userId);
        return emitter;
    }

    /**
     * 운동 세션 구독
     */
    public SseEmitter subscribeToWorkout(Long workoutId, Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        workoutEmitters.computeIfAbsent(workoutId, k -> new CopyOnWriteArrayList<>())
            .add(emitter);

        emitter.onCompletion(() -> unsubscribeFromWorkout(workoutId, emitter));
        emitter.onTimeout(() -> unsubscribeFromWorkout(workoutId, emitter));
        emitter.onError((e) -> unsubscribeFromWorkout(workoutId, emitter));

        // 구독 확인 이벤트
        try {
            emitter.send(SseEmitter.event()
                .name("subscribe")
                .data(Map.of("workoutId", workoutId, "userId", userId)));
        } catch (IOException e) {
            log.error("Failed to send subscribe event", e);
        }

        log.info("User {} subscribed to workout {}", userId, workoutId);
        return emitter;
    }

    /**
     * 그룹 활동 구독
     */
    public SseEmitter subscribeToGroup(Long groupId, Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        groupEmitters.computeIfAbsent(groupId, k -> new CopyOnWriteArrayList<>())
            .add(emitter);

        emitter.onCompletion(() -> unsubscribeFromGroup(groupId, emitter));
        emitter.onTimeout(() -> unsubscribeFromGroup(groupId, emitter));
        emitter.onError((e) -> unsubscribeFromGroup(groupId, emitter));

        // 구독 확인 이벤트
        try {
            emitter.send(SseEmitter.event()
                .name("subscribe")
                .data(Map.of("groupId", groupId, "userId", userId)));
        } catch (IOException e) {
            log.error("Failed to send subscribe event", e);
        }

        log.info("User {} subscribed to group {}", userId, groupId);
        return emitter;
    }

    /**
     * 피드 실시간 구독
     */
    public SseEmitter subscribeToFeed(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        feedEmitters.add(emitter);

        emitter.onCompletion(() -> feedEmitters.remove(emitter));
        emitter.onTimeout(() -> feedEmitters.remove(emitter));
        emitter.onError((e) -> feedEmitters.remove(emitter));

        // 구독 확인 이벤트
        try {
            emitter.send(SseEmitter.event()
                .name("subscribe")
                .data(Map.of("feed", true, "userId", userId)));
        } catch (IOException e) {
            log.error("Failed to send subscribe event", e);
        }

        log.info("User {} subscribed to feed", userId);
        return emitter;
    }

    /**
     * 특정 사용자에게 이벤트 전송
     * @return SSE 전송 성공 여부 (true: 연결되어 있음, false: 연결 없음)
     */
    public boolean sendToUser(Long userId, String eventName, Object data) {
        String emitterId = makeUserEmitterId(userId);
        SseEmitter emitter = userEmitters.get(emitterId);

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .id(String.valueOf(System.currentTimeMillis()))
                    .name(eventName)
                    .data(data));
                log.debug("Event sent to user {}: {}", userId, eventName);
                return true;
            } catch (IOException e) {
                log.error("Failed to send event to user {}", userId, e);
                removeUserEmitter(emitterId);
                return false;
            }
        }
        return false;
    }

    /**
     * 운동 구독자들에게 이벤트 전송
     */
    public void sendToWorkoutSubscribers(Long workoutId, String eventName, Object data) {
        CopyOnWriteArrayList<SseEmitter> emitters = workoutEmitters.get(workoutId);

        if (emitters != null) {
            emitters.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
                } catch (IOException e) {
                    log.error("Failed to send workout event", e);
                    emitters.remove(emitter);
                }
            });
            log.debug("Event sent to workout {} subscribers: {}", workoutId, eventName);
        }
    }

    /**
     * 그룹 구독자들에게 이벤트 전송
     */
    public void sendToGroupSubscribers(Long groupId, String eventName, Object data) {
        CopyOnWriteArrayList<SseEmitter> emitters = groupEmitters.get(groupId);

        if (emitters != null) {
            emitters.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
                } catch (IOException e) {
                    log.error("Failed to send group event", e);
                    emitters.remove(emitter);
                }
            });
            log.debug("Event sent to group {} subscribers: {}", groupId, eventName);
        }
    }

    /**
     * 전체 피드 구독자들에게 이벤트 전송
     */
    public void sendToFeedSubscribers(String eventName, Object data) {
        feedEmitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
            } catch (IOException e) {
                log.error("Failed to send feed event", e);
                feedEmitters.remove(emitter);
            }
        });
        log.debug("Event sent to feed subscribers: {}", eventName);
    }

    /**
     * 사용자 Emitter ID 생성
     */
    private String makeUserEmitterId(Long userId) {
        return userId + "_" + System.currentTimeMillis();
    }

    /**
     * 사용자 Emitter 제거
     */
    private void removeUserEmitter(String emitterId) {
        userEmitters.remove(emitterId);
        log.debug("SSE emitter removed: {}", emitterId);
    }

    /**
     * 운동 구독 해제
     */
    private void unsubscribeFromWorkout(Long workoutId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = workoutEmitters.get(workoutId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                workoutEmitters.remove(workoutId);
            }
        }
    }

    /**
     * 그룹 구독 해제
     */
    private void unsubscribeFromGroup(Long groupId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = groupEmitters.get(groupId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                groupEmitters.remove(groupId);
            }
        }
    }
}
package com.sharegym.sharegym_server.dto.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 운동 업데이트 알림 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkoutUpdateNotification {
    private Long workoutId;
    private Long userId;
    private String userName;
    private String updateType; // SET_COMPLETE, EXERCISE_ADDED, REST_STARTED
    private Object details; // 업데이트 상세 정보

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
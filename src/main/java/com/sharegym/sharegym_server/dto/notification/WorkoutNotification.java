package com.sharegym.sharegym_server.dto.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 운동 알림 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkoutNotification {
    private Long workoutId;
    private Long userId;
    private String userName;
    private String workoutName;
    private String status; // STARTED, COMPLETED, CANCELLED
    private Integer totalSets;
    private Integer totalVolume;
    private Integer duration; // seconds
    private String message;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
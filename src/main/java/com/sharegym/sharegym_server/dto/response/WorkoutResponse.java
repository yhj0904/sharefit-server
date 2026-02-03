package com.sharegym.sharegym_server.dto.response;

import com.sharegym.sharegym_server.entity.Workout;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 운동 세션 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String workoutName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private Double totalWeight;
    private Integer totalSets;
    private Integer totalReps;
    private Integer caloriesBurned;
    private String note;
    private String status;
    private List<WorkoutExerciseResponse> exercises;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity를 DTO로 변환
     */
    public static WorkoutResponse from(Workout workout) {
        return WorkoutResponse.builder()
            .id(workout.getId())
            .userId(workout.getUser().getId())
            .userName(workout.getUser().getDisplayName())
            .workoutName(workout.getWorkoutName())
            .startTime(workout.getStartTime())
            .endTime(workout.getEndTime())
            .durationMinutes(workout.getDurationMinutes())
            .totalWeight(workout.getTotalWeight())
            .totalSets(workout.getTotalSets())
            .totalReps(workout.getTotalReps())
            .caloriesBurned(workout.getCaloriesBurned())
            .note(workout.getNote())
            .status(workout.getStatus().name())
            .exercises(workout.getExercises().stream()
                .map(WorkoutExerciseResponse::from)
                .collect(Collectors.toList()))
            .createdAt(workout.getCreatedAt())
            .updatedAt(workout.getUpdatedAt())
            .build();
    }

    /**
     * Entity를 간단한 DTO로 변환 (운동 정보 제외)
     */
    public static WorkoutResponse fromSimple(Workout workout) {
        return WorkoutResponse.builder()
            .id(workout.getId())
            .userId(workout.getUser().getId())
            .userName(workout.getUser().getDisplayName())
            .workoutName(workout.getWorkoutName())
            .startTime(workout.getStartTime())
            .endTime(workout.getEndTime())
            .durationMinutes(workout.getDurationMinutes())
            .totalWeight(workout.getTotalWeight())
            .totalSets(workout.getTotalSets())
            .totalReps(workout.getTotalReps())
            .caloriesBurned(workout.getCaloriesBurned())
            .note(workout.getNote())
            .status(workout.getStatus().name())
            .createdAt(workout.getCreatedAt())
            .updatedAt(workout.getUpdatedAt())
            .build();
    }
}
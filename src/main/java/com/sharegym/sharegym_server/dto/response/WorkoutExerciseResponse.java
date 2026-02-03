package com.sharegym.sharegym_server.dto.response;

import com.sharegym.sharegym_server.common.ExerciseIdMapper;
import com.sharegym.sharegym_server.entity.WorkoutExercise;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 운동별 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutExerciseResponse {

    private Long id;
    private String exerciseId; // 클라이언트 운동 ID
    private String exerciseName;
    private String exerciseNameKo;
    private String category;
    private String unit;
    private Integer orderIndex;
    private Integer targetSets;
    private Integer targetReps;
    private Double targetWeight;
    private Integer completedSets;
    private Double totalVolume;
    private String note;
    private List<SetResponse> sets;

    /**
     * Entity를 DTO로 변환
     */
    public static WorkoutExerciseResponse from(WorkoutExercise workoutExercise) {
        ExerciseIdMapper mapper = new ExerciseIdMapper();
        String clientExerciseId = mapper.toClientId(workoutExercise.getExercise().getId());

        return WorkoutExerciseResponse.builder()
            .id(workoutExercise.getId())
            .exerciseId(clientExerciseId)
            .exerciseName(workoutExercise.getExercise().getName())
            .exerciseNameKo(workoutExercise.getExercise().getNameKo())
            .category(workoutExercise.getExercise().getCategory().name())
            .unit(workoutExercise.getExercise().getUnit().name())
            .orderIndex(workoutExercise.getOrderIndex())
            .targetSets(workoutExercise.getTargetSets())
            .targetReps(workoutExercise.getTargetReps())
            .targetWeight(workoutExercise.getTargetWeight())
            .completedSets(workoutExercise.getCompletedSets())
            .totalVolume(workoutExercise.getTotalVolume())
            .note(workoutExercise.getNote())
            .sets(workoutExercise.getSets().stream()
                .map(SetResponse::from)
                .collect(Collectors.toList()))
            .build();
    }
}
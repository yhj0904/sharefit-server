package com.sharegym.sharegym_server.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 루틴 내 운동 정보 Entity
 */
@Entity
@Table(name = "routine_exercises",
    indexes = {
        @Index(name = "idx_routine_id", columnList = "routine_id"),
        @Index(name = "idx_exercise_id", columnList = "exercise_id")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutineExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex; // 운동 순서

    @Column(name = "target_sets")
    private Integer targetSets; // 목표 세트 수

    @Column(name = "target_reps")
    private Integer targetReps; // 목표 반복 횟수

    @Column(name = "target_weight")
    private Double targetWeight; // 목표 무게

    @Column(name = "rest_seconds")
    @Builder.Default
    private Integer restSeconds = 60; // 휴식 시간 (초)

    @Column(columnDefinition = "TEXT")
    private String note; // 운동별 메모

    /**
     * 운동을 WorkoutExercise로 변환 (실제 운동 시작 시)
     */
    public WorkoutExercise toWorkoutExercise(Workout workout) {
        return WorkoutExercise.builder()
            .workout(workout)
            .exercise(this.exercise)
            .orderIndex(this.orderIndex)
            .targetSets(this.targetSets)
            .targetReps(this.targetReps)
            .targetWeight(this.targetWeight)
            .note(this.note)
            .build();
    }
}
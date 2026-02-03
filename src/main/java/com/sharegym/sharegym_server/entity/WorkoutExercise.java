package com.sharegym.sharegym_server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 운동 세션 내의 개별 운동 Entity
 */
@Entity
@Table(name = "workout_exercises",
    indexes = {
        @Index(name = "idx_workout_id", columnList = "workout_id"),
        @Index(name = "idx_exercise_id", columnList = "exercise_id")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutExercise extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

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

    @Column(columnDefinition = "TEXT")
    private String note; // 운동별 메모

    // 연관 관계 - 세트 정보
    @OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("setNumber ASC")
    @Builder.Default
    private List<WorkoutSet> workoutSets = new ArrayList<>();

    /**
     * 세트 추가
     */
    public void addSet(WorkoutSet set) {
        workoutSets.add(set);
        set.setWorkoutExercise(this);
        set.setSetNumber(workoutSets.size());
    }

    /**
     * 세트 제거
     */
    public void removeSet(WorkoutSet set) {
        workoutSets.remove(set);
        set.setWorkoutExercise(null);
        // 세트 번호 재정렬
        for (int i = 0; i < workoutSets.size(); i++) {
            workoutSets.get(i).setSetNumber(i + 1);
        }
    }

    /**
     * 완료된 세트 수 계산
     */
    public int getCompletedSets() {
        return (int) workoutSets.stream()
            .filter(WorkoutSet::getIsCompleted)
            .count();
    }

    /**
     * 총 볼륨 계산 (무게 x 반복 횟수)
     */
    public double getTotalVolume() {
        return workoutSets.stream()
            .filter(WorkoutSet::getIsCompleted)
            .mapToDouble(set -> {
                if (set.getWeight() != null && set.getReps() != null) {
                    return set.getWeight() * set.getReps();
                }
                return 0.0;
            })
            .sum();
    }

    /**
     * 편의 메서드 - 세트 목록 반환
     */
    public List<WorkoutSet> getSets() {
        return this.workoutSets;
    }
}
package com.sharegym.sharegym_server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 운동 세션 Entity
 */
@Entity
@Table(name = "workouts",
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_start_time", columnList = "start_time")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workout extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "workout_name", length = 100)
    private String workoutName; // 운동 세션 이름

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration")
    private Integer duration; // 운동 시간 (초 단위)

    @Column(name = "duration_minutes")
    private Integer durationMinutes; // 운동 시간 (분) - 기존 호환성

    @Column(name = "total_weight")
    @Builder.Default
    private Double totalWeight = 0.0; // 총 중량 (kg) - 기존 호환성

    @Column(name = "total_volume")
    @Builder.Default
    private Integer totalVolume = 0; // 총 볼륨 (프론트엔드 형식)

    @Column(name = "total_sets")
    @Builder.Default
    private Integer totalSets = 0; // 총 세트 수

    @Column(name = "total_reps")
    @Builder.Default
    private Integer totalReps = 0; // 총 반복 횟수

    @Column(name = "calories")
    private Integer calories; // 소모 칼로리 (프론트엔드 형식)

    @Column(name = "calories_burned")
    private Integer caloriesBurned; // 소모 칼로리 - 기존 호환성

    @Column(columnDefinition = "TEXT")
    private String note; // 운동 메모

    @Column(name = "completion_image_url")
    private String completionImageUrl; // 운동 완료 시 찍은 사진

    @ElementCollection
    @CollectionTable(name = "workout_images", joinColumns = @JoinColumn(name = "workout_id"))
    @Column(name = "image_url")
    @OrderColumn(name = "image_order")
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>(); // 운동 중 촬영한 이미지들

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private WorkoutStatus status = WorkoutStatus.IN_PROGRESS;

    // 연관 관계 - 운동별 정보
    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkoutExercise> workoutExercises = new ArrayList<>();

    // 연관 관계 - 피드 (운동 완료 시 생성)
    @OneToOne(mappedBy = "workout")
    private Feed feed;

    /**
     * 운동 상태 Enum
     */
    public enum WorkoutStatus {
        IN_PROGRESS,  // 진행 중
        COMPLETED,    // 완료
        CANCELLED     // 취소
    }

    /**
     * 운동 완료 처리
     */
    public void complete() {
        this.status = WorkoutStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.duration = (int) java.time.Duration.between(startTime, endTime).toSeconds();
        this.durationMinutes = this.duration / 60;
        calculateTotals();
    }

    /**
     * 통계 계산
     */
    private void calculateTotals() {
        this.totalSets = 0;
        this.totalReps = 0;
        this.totalWeight = 0.0;
        this.totalVolume = 0;

        for (WorkoutExercise exercise : workoutExercises) {
            for (WorkoutSet set : exercise.getWorkoutSets()) {
                if (set.getIsCompleted()) {
                    this.totalSets++;
                    if (set.getReps() != null) {
                        this.totalReps += set.getReps();
                    }
                    if (set.getWeight() != null && set.getReps() != null) {
                        double volume = set.getWeight() * set.getReps();
                        this.totalWeight += volume;
                        this.totalVolume += (int) Math.round(volume);
                    }
                }
            }
        }
    }

    /**
     * 운동 추가
     */
    public void addExercise(WorkoutExercise exercise) {
        workoutExercises.add(exercise);
        exercise.setWorkout(this);
    }

    /**
     * 운동 제거
     */
    public void removeExercise(WorkoutExercise exercise) {
        workoutExercises.remove(exercise);
        exercise.setWorkout(null);
    }

    /**
     * 편의 메서드 - 운동 목록 반환
     */
    public List<WorkoutExercise> getExercises() {
        return this.workoutExercises;
    }
}
package com.sharegym.sharegym_server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 운동 세트 정보 Entity
 */
@Entity
@Table(name = "workout_sets",
    indexes = {
        @Index(name = "idx_workout_exercise_id", columnList = "workout_exercise_id")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutSet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_exercise_id", nullable = false)
    private WorkoutExercise workoutExercise;

    @Column(name = "set_number", nullable = false)
    private Integer setNumber; // 세트 번호

    @Column
    private Integer reps; // 반복 횟수

    @Column
    private Double weight; // 무게 (kg) - 유산소의 경우 null 가능

    @Column
    private Double distance; // 거리 (km) - 유산소 운동용

    @Column(name = "duration")
    private Integer duration; // 지속 시간 (초) - 프론트엔드 형식

    @Column(name = "duration_seconds")
    private Integer durationSeconds; // 지속 시간 (초) - 기존 호환성

    @Column
    private Integer level; // 레벨/강도 - 머신 운동용

    @Enumerated(EnumType.STRING)
    @Column(name = "set_type", length = 20)
    @Builder.Default
    private SetType setType = SetType.NORMAL; // 세트 타입

    @Column(name = "is_warmup")
    @Builder.Default
    private Boolean isWarmup = false; // 워밍업 여부

    @Column(name = "is_failure")
    @Builder.Default
    private Boolean isFailure = false; // 실패 세트 여부

    @Column(name = "is_dropset")
    @Builder.Default
    private Boolean isDropset = false; // 드롭세트 여부

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = false; // 완료 여부

    @Column(name = "completed_at")
    private LocalDateTime completedAt; // 완료 시간

    @Column(name = "rest_seconds")
    private Integer restSeconds; // 휴식 시간 (초)

    @Column(columnDefinition = "TEXT")
    private String note; // 세트별 메모

    /**
     * 세트 타입 Enum
     */
    public enum SetType {
        NORMAL,     // 일반 세트
        WARMUP,     // 워밍업
        DROP,       // 드롭세트
        SUPER,      // 슈퍼세트
        FAILURE     // 실패지점까지
    }

    /**
     * 세트 완료 처리
     */
    public void complete() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 볼륨 계산 (무게 x 반복횟수)
     */
    public Double getVolume() {
        if (weight != null && reps != null) {
            return weight * reps;
        }
        return 0.0;
    }

    /**
     * 1RM 추정 (Epley 공식)
     */
    public Double getEstimated1RM() {
        if (weight != null && reps != null && reps > 1) {
            return weight * (1 + reps / 30.0);
        }
        return weight;
    }
}
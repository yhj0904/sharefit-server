package com.sharegym.sharegym_server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 운동 루틴 템플릿 Entity
 */
@Entity
@Table(name = "routines",
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Routine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 루틴 생성자

    @Column(nullable = false, length = 100)
    private String name; // 루틴 이름

    @Column(columnDefinition = "TEXT")
    private String description; // 루틴 설명

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false; // 공개 여부

    @Column(name = "is_favorite")
    @Builder.Default
    private Boolean isFavorite = false; // 즐겨찾기 여부

    @Column(name = "use_count")
    @Builder.Default
    private Integer useCount = 0; // 사용 횟수

    @Column(name = "copy_count")
    @Builder.Default
    private Integer copyCount = 0; // 복사된 횟수

    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // 예상 소요 시간 (분)

    // 연관 관계 - 루틴 운동 목록
    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<RoutineExercise> exercises = new ArrayList<>();

    /**
     * 루틴 사용 카운트 증가
     */
    public void incrementUseCount() {
        this.useCount++;
    }

    /**
     * 루틴 복사 카운트 증가
     */
    public void incrementCopyCount() {
        this.copyCount++;
    }

    /**
     * 즐겨찾기 토글
     */
    public void toggleFavorite() {
        this.isFavorite = !this.isFavorite;
    }

    /**
     * 운동 추가
     */
    public void addExercise(RoutineExercise exercise) {
        exercises.add(exercise);
        exercise.setRoutine(this);
    }

    /**
     * 운동 제거
     */
    public void removeExercise(RoutineExercise exercise) {
        exercises.remove(exercise);
        exercise.setRoutine(null);
    }

    /**
     * 루틴 복사 (새로운 사용자용)
     */
    public Routine copyForUser(User newUser) {
        Routine copy = Routine.builder()
            .user(newUser)
            .name(this.name + " (복사본)")
            .description(this.description)
            .isPublic(false)
            .estimatedDuration(this.estimatedDuration)
            .build();

        // 운동 목록 복사
        for (RoutineExercise exercise : this.exercises) {
            RoutineExercise exerciseCopy = RoutineExercise.builder()
                .routine(copy)
                .exercise(exercise.getExercise())
                .orderIndex(exercise.getOrderIndex())
                .targetSets(exercise.getTargetSets())
                .targetReps(exercise.getTargetReps())
                .targetWeight(exercise.getTargetWeight())
                .restSeconds(exercise.getRestSeconds())
                .note(exercise.getNote())
                .build();
            copy.addExercise(exerciseCopy);
        }

        this.incrementCopyCount();
        return copy;
    }
}
package com.sharegym.sharegym_server.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 운동 종류 마스터 데이터 Entity
 * 클라이언트의 문자열 ID를 숫자 ID로 매핑
 */
@Entity
@Table(name = "exercises",
    indexes = {
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_client_id", columnList = "client_id")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Exercise {

    @Id
    private Integer id; // 카테고리별 숫자 ID (1000번대, 2000번대 등)

    @Column(name = "client_id", nullable = false, unique = true, length = 50)
    private String clientId; // 클라이언트 문자열 ID (bench-press, squat 등)

    @Column(name = "exercise_name", nullable = false, length = 100)
    private String exerciseName; // 영문 이름

    @Column(name = "exercise_name_ko", nullable = false, length = 100)
    private String exerciseNameKo; // 한글 이름

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category; // 카테고리

    @Column(name = "muscle_groups", length = 200)
    private String muscleGroups; // 주요 근육 그룹 (쉼표로 구분)

    @Column(length = 100)
    private String equipment; // 필요 장비

    @Column(length = 50)
    private String icon; // 아이콘 이름

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Unit unit = Unit.KG; // 단위 (kg, km, level, reps)

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true; // 활성 상태

    /**
     * 운동 카테고리 Enum
     */
    public enum Category {
        CHEST,      // 가슴 (1000번대)
        BACK,       // 등 (2000번대)
        SHOULDERS,  // 어깨 (3000번대)
        LEGS,       // 하체 (4000번대)
        ARMS,       // 팔 (5000번대)
        ABS,        // 복근 (6000번대)
        CARDIO,     // 유산소 (7000번대)
        BODYWEIGHT  // 맨몸 (8000번대)
    }

    /**
     * 운동 단위 Enum
     */
    public enum Unit {
        KG,     // 무게 (기본값)
        KM,     // 거리
        LEVEL,  // 레벨/강도
        REPS    // 횟수
    }

    /**
     * 카테고리별 ID 범위 확인
     */
    public static int getCategoryStartId(Category category) {
        return switch (category) {
            case CHEST -> 1000;
            case BACK -> 2000;
            case SHOULDERS -> 3000;
            case LEGS -> 4000;
            case ARMS -> 5000;
            case ABS -> 6000;
            case CARDIO -> 7000;
            case BODYWEIGHT -> 8000;
        };
    }

    /**
     * 편의 메서드 - 영문 이름 반환
     */
    public String getName() {
        return this.exerciseName;
    }

    /**
     * 편의 메서드 - 한글 이름 반환
     */
    public String getNameKo() {
        return this.exerciseNameKo;
    }
}
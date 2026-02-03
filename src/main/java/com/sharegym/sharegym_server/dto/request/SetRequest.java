package com.sharegym.sharegym_server.dto.request;

import com.sharegym.sharegym_server.entity.WorkoutSet;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 세트 정보 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetRequest {

    @NotNull(message = "세트 번호는 필수입니다.")
    @Positive(message = "세트 번호는 양수여야 합니다.")
    private Integer setNumber;

    private Integer reps;

    private Double weight;

    private Double distance; // km (유산소 운동용)

    private Integer durationSeconds; // 초 (유산소 운동용)

    private Integer level; // 레벨/강도 (머신 운동용)

    @Builder.Default
    private WorkoutSet.SetType setType = WorkoutSet.SetType.NORMAL;

    @Builder.Default
    private Boolean isCompleted = false;

    private Integer restSeconds;

    private String note;
}
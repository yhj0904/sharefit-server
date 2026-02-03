package com.sharegym.sharegym_server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 운동 추가 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddExerciseRequest {

    @NotBlank(message = "운동 ID는 필수입니다.")
    private String exerciseId; // 클라이언트 운동 ID (bench-press, squat 등)

    @NotNull(message = "순서는 필수입니다.")
    @Positive(message = "순서는 양수여야 합니다.")
    private Integer orderIndex;

    private Integer targetSets;

    private Integer targetReps;

    private Double targetWeight;

    private String note;
}
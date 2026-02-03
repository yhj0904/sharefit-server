package com.sharegym.sharegym_server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 운동 세션 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkoutRequest {

    @NotBlank(message = "운동 이름은 필수입니다.")
    @Size(max = 100, message = "운동 이름은 100자 이내여야 합니다.")
    private String workoutName;

    private LocalDateTime startTime; // null이면 현재 시간으로 설정

    @Size(max = 1000, message = "메모는 1000자 이내여야 합니다.")
    private String note;

    private Long routineId; // 루틴 기반으로 시작하는 경우 (선택적)
}
package com.sharegym.sharegym_server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 응원 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheerRequest {

    @NotBlank(message = "응원 메시지는 필수입니다.")
    @Size(max = 200, message = "응원 메시지는 200자 이내여야 합니다.")
    private String message;

    private String emoji; // 선택적 이모지
}
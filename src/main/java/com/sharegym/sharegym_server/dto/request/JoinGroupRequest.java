package com.sharegym.sharegym_server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 그룹 가입 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinGroupRequest {

    @NotBlank(message = "초대 코드는 필수입니다.")
    @Pattern(regexp = "^[A-Z0-9]{6}$", message = "초대 코드는 6자리 영문 대문자와 숫자여야 합니다.")
    private String inviteCode;
}
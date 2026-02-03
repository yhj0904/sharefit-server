package com.sharegym.sharegym_server.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 100, message = "이메일은 100자 이내여야 합니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
        message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @NotBlank(message = "사용자명은 필수입니다.")
    @Pattern(regexp = "^[a-z0-9_]{3,30}$",
        message = "사용자명은 3-30자의 영소문자, 숫자, 언더스코어만 사용 가능합니다.")
    private String username;

    @NotBlank(message = "표시 이름은 필수입니다.")
    @Size(min = 1, max = 50, message = "표시 이름은 1-50자 이내여야 합니다.")
    private String displayName;

    @Size(max = 500, message = "자기소개는 500자 이내여야 합니다.")
    private String bio;
}
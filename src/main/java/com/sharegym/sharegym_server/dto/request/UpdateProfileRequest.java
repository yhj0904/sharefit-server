package com.sharegym.sharegym_server.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 프로필 수정 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Pattern(regexp = "^[a-z0-9_]{3,30}$",
        message = "사용자명은 3-30자의 영소문자, 숫자, 언더스코어만 사용 가능합니다.")
    private String username;

    @Size(min = 1, max = 50, message = "표시 이름은 1-50자 이내여야 합니다.")
    private String displayName;

    @Size(max = 500, message = "자기소개는 500자 이내여야 합니다.")
    private String bio;

    private String profileImageUrl;

    private String fcmToken; // FCM 토큰 업데이트
}